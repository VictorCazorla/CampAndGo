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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.tfg.campandgo.data.model.Place
import com.tfg.campandgo.data.model.Prediction
import com.tfg.campandgo.ui.viewmodel.MapsViewModel


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
            viewModel.fetchNearbyPlaces(location, apiKey)
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
        nearbyPlaces = viewModel.nearbyPlaces,
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
    nearbyPlaces: List<Place>,
    viewModel: MapsViewModel
) {
    val cameraPositionState = rememberCameraPositionState()
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }
    val context = LocalContext.current
    var showNearbyPlaces by remember { mutableStateOf(false) } // Estado para controlar la visibilidad

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
        // GoogleMap con condicional para mostrar ubicaciones solo cuando showNearbyPlaces sea true
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                minZoomPreference = 10f,
                maxZoomPreference = 20f
            ),
            uiSettings = uiSettings,
            onMapClick = handleMapClick // Detectar el clic en el mapa
        ) {
            // Marcador para la ubicación actual (siempre visible)
            currentLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Ubicación actual",
                    snippet = "Lat: ${"%.4f".format(location.latitude)}, Lng: ${"%.4f".format(location.longitude)}"
                )
            }

            // Siempre mostrar el marcador del lugar seleccionado si existe
            viewModel.selectedLocation.value?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Ubicación seleccionada",
                    snippet = "Lat: ${"%.4f".format(location.latitude)}, Lng: ${"%.4f".format(location.longitude)}"
                )
            }

            // Solo mostrar lugares cercanos si showNearbyPlaces es true
            if (showNearbyPlaces) {
                nearbyPlaces.forEach { place ->
                    place.geometry?.location?.let { location ->
                        Marker(
                            state = MarkerState(position = LatLng(location.lat, location.lng)),
                            title = place.name,
                            snippet = place.vicinity
                        )
                    }
                }
            }
        }

        // Mostrar detalles del lugar seleccionado
        viewModel.placeDetails.value?.let { place ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre: ${place.name}")
                Text("Dirección: ${place.formatted_address}")
                Text("Tipos: ${place.types?.joinToString() ?: "Desconocido"}")
            }
        }

        // Botón para mostrar/ocultar la lista de lugares cercanos
        Button(
            onClick = { showNearbyPlaces = !showNearbyPlaces }, // Cambia el estado
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(if (showNearbyPlaces) "Ocultar lugares cercanos" else "Mostrar lugares cercanos")
        }

        // Lista de lugares cercanos (solo si showNearbyPlaces es true)
        if (showNearbyPlaces) {
            LazyColumn(modifier = Modifier.height(200.dp)) {
                items(nearbyPlaces) { place ->
                    NearbyPlaceItem(place = place, onPlaceSelected = { selectedPlace ->
                        val latLng = selectedPlace.geometry?.location?.let { LatLng(it.lat, selectedPlace.geometry.location.lng) }

                        // Actualizar la ubicación seleccionada en ViewModel
                        viewModel.selectedLocation.value = latLng

                        // Centrar el mapa en la ubicación del lugar seleccionado
                        latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
                            ?.let { cameraPositionState.move(it) }

                        // Obtener detalles del lugar seleccionado
                        val apiKey = getApiKeyFromManifest(context) ?: ""
                        viewModel.getPlaceDetailsFromPlaceId(selectedPlace.placeId, apiKey)
                    })
                }
            }
        }

        // Barra de búsqueda y sugerencias
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = prediction.structured_formatting.main_text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = prediction.structured_formatting.secondary_text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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

@Composable
private fun NearbyPlaceItem(place: Place, onPlaceSelected: (Place) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onPlaceSelected(place) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = place.name, style = MaterialTheme.typography.titleMedium)
            Text(text = place.vicinity ?: "Dirección no disponible", style = MaterialTheme.typography.bodySmall)
            Text(text = "Rating: ${place.rating ?: "No disponible"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}





