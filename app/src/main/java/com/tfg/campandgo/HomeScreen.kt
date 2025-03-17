import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.tfg.campandgo.MapsViewModel
import com.tfg.campandgo.Prediction
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import java.io.IOException
import java.util.Locale

@Composable
fun HomeScreen() {
    val viewModel: MapsViewModel = viewModel()
    val context = LocalContext.current
    val apiKey = remember { getApiKeyFromManifest(context) }
    var searchQuery by remember { mutableStateOf("") }

    if (apiKey == null) {
        ErrorScreen(message = "Error: API Key no configurada en el Manifest")
        return
    }

    PermissionHandler(viewModel)

    if (viewModel.hasLocationPermission.value) {
        LocationFetcher { location ->
            viewModel.selectedLocation.value = location
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            viewModel.searchLocations(searchQuery, apiKey)
        }
    }

    MapScreen(
        currentLocation = viewModel.selectedLocation.value,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onSearch = { placeId -> viewModel.getLocationDetails(placeId, apiKey) },
        searchSuggestions = viewModel.searchSuggestions,
        errorMessage = viewModel.errorMessage.value,
        viewModel = viewModel
    )
}

@Composable
private fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

private fun getApiKeyFromManifest(context: Context): String? {
    return try {
        val appInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        appInfo.metaData.getString("com.google.android.geo.API_KEY")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
private fun MapScreen(
    currentLocation: LatLng?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchSuggestions: List<Prediction>,
    errorMessage: String?,
    viewModel: MapsViewModel
) {
    val cameraPositionState = rememberCameraPositionState()
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }
    val context = LocalContext.current

    // Función para centrar el mapa en la ubicación actual
    val centerMap: () -> Unit = {
        currentLocation?.let {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    // Función para manejar la búsqueda
    val handleSearch: () -> Unit = {
        if (searchQuery.isNotEmpty()) {
            viewModel.geocodeAddress(searchQuery, context) { latLng ->
                latLng?.let {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }
        }
    }

    /**
     * Maneja los clics en el mapa
     */
    val handleMapClick: (LatLng) -> Unit = { latLng ->
        viewModel.selectedLocation.value = latLng

        // Obtiene de geocodeAddress los detalles sobre la ubicación
        viewModel.geocodeResultAddress("${latLng.latitude},${latLng.longitude}", context) { geocodeResult ->
            geocodeResult?.let {
                val lat = it.geometry.location.lat
                val lng = it.geometry.location.lng
                Log.e("MapsViewModel", "Ubicación: Lat: $lat, Lng: $lng")
                Toast.makeText(context, "Ubicación: Lat: $lat, Lng: $lng", Toast.LENGTH_LONG).show()

                // Búsqueda del placeId
                val placeId = geocodeResult.placeId
                val apiKey = getApiKeyFromManifest(context) ?: ""
                viewModel.getPlaceDetailsFromPlaceId(placeId, apiKey)  // Usamos el placeId
            } ?: run {
                Log.e("MapsViewModel", "Ubicación desconocida")
                Toast.makeText(context, "Ubicación desconocida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                minZoomPreference = 10f,
                maxZoomPreference = 20f
            ),
            uiSettings = uiSettings,
            onMapClick = handleMapClick         // Detectar el clic en el mapa
        ) {
            currentLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Ubicación seleccionada",
                    snippet = "Lat: ${"%.4f".format(location.latitude)}, Lng: ${"%.4f".format(location.longitude)}"
                )
            }

            // Marcador dibujado en el selectedLocation
            viewModel.selectedLocation.value?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Marcador",
                    snippet = "Lat: ${"%.4f".format(location.latitude)}, Lng: ${"%.4f".format(location.longitude)}"
                )
            }
        }

        // Mostrar detalles del lugar
        viewModel.placeDetails.value?.let { place ->
            Text("Nombre: ${place.name}")
            Text("Dirección: ${place.formatted_address}")
            Text("Tipos: ${place.types?.joinToString() ?: "Desconocido"}")
        }

        SearchBarWithSuggestions(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            suggestions = searchSuggestions,
            errorMessage = errorMessage,
            onSuggestionSelected = { prediction ->
                onSearch(prediction.place_id)
                onSearchQueryChange(prediction.description)
            },
            onCenterMap = centerMap,
            onSearch = handleSearch
        )
    }
}

@Composable
private fun SearchBarWithSuggestions(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    suggestions: List<Prediction>,
    errorMessage: String?,
    onSuggestionSelected: (Prediction) -> Unit,
    onCenterMap: () -> Unit,
    onSearch: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Buscar ubicación") },
                modifier = Modifier.weight(1f)
            )

            // Botón de lupa para buscar la ubicación
            IconButton(
                onClick = onSearch,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar ubicación",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Botón para centrar el mapa en la ubicación actual
            IconButton(
                onClick = onCenterMap,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar mapa",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (suggestions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(suggestions) { prediction ->
                    SuggestionItem(
                        prediction = prediction,
                        onSuggestionSelected = onSuggestionSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    prediction: Prediction,
    onSuggestionSelected: (Prediction) -> Unit
) {
    TextButton(
        onClick = { onSuggestionSelected(prediction) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = prediction.structured_formatting.main_text,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = prediction.structured_formatting.secondary_text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PermissionHandler(viewModel: MapsViewModel) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.hasLocationPermission.value = isGranted
        if (!isGranted) showPermissionDialog = true
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.hasLocationPermission.value = true
            }
            else -> permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permiso requerido") },
            text = { Text("Necesitamos acceso a tu ubicación para mostrar el mapa") },
            confirmButton = {
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    showPermissionDialog = false
                }) { Text("Reintentar") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun LocationFetcher(onLocationFetched: (LatLng) -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { onLocationFetched(LatLng(it.latitude, it.longitude)) }
            }
        }
    }
}

// Añade esta función en tu MapsViewModel
fun geocodeAddress(address: String, context: Context, onResult: (LatLng?) -> Unit) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocationName(address, 1)
        if (addresses?.isNotEmpty() == true) {
            val location = addresses[0]
            val latLng = LatLng(location.latitude, location.longitude)
            onResult(latLng)
        } else {
            onResult(null)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        onResult(null)
    }
}