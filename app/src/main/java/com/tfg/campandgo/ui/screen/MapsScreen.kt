package com.tfg.campandgo.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import com.tfg.campandgo.R
import com.tfg.campandgo.data.model.Place
import com.tfg.campandgo.data.model.Prediction
import com.tfg.campandgo.ui.component.NearbyPlaceItem
import com.tfg.campandgo.ui.component.PlaceDetailsSection
import com.tfg.campandgo.ui.component.SearchBarWithSuggestions
import com.tfg.campandgo.ui.component.ToggleButtonGrid
import com.tfg.campandgo.ui.viewmodel.MapsViewModel
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun MapScreen(
    navigator: NavController,
    currentLocation: LatLng?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchSuggestions: List<Prediction>,
    nearbyPlaces: List<Place>,
    viewModel: MapsViewModel
) {
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val apiKey = remember { getApiKeyFromManifest(context) }
    var showNearbyPlaces by remember { mutableStateOf(false) }
    var selectedPlaceId by remember { mutableStateOf<String?>(null) }
    var termFilterList by remember { mutableStateOf(mutableListOf<String>()) }
    var showFirebasePlaces by remember { mutableStateOf(false) }
    val firebaseCamperSites = viewModel.firebaseCamperSites
    val user = Firebase.auth.currentUser

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location, 15f))
            if (apiKey != null) viewModel.clearSearchLocations(context)
        }
    }

    val handleFilterSelected: (List<String>) -> Unit = { filters ->
        termFilterList = filters.toMutableList()
        if (termFilterList.isEmpty()) {
            viewModel.cleanNearbyPlaces()
            showNearbyPlaces = false
            viewModel.placeDetails.value = null
        } else if (apiKey != null && showNearbyPlaces) {
            viewModel.cleanNearbyPlaces()
            termFilterList.forEach { filter ->
                viewModel.fetchNearbyPlaces(viewModel.selectedLocation.value!!, apiKey, context, filter)
            }
        }
    }

    val handleSearch: () -> Unit = {
        if (searchQuery.isNotEmpty()) {
            viewModel.geocodeAddress(searchQuery, context) { latLng ->
                latLng?.let {
                    viewModel.selectedLocation.value = it
                    if (apiKey != null) {
                        viewModel.cleanNearbyPlaces()
                        termFilterList.forEach { filter ->
                            viewModel.fetchNearbyPlaces(it, apiKey, context, filter)
                        }
                    }
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }
        }
    }

    val centerMap: () -> Unit = {
        currentLocation?.let {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                isIndoorEnabled = true,
                isTrafficEnabled = true,
                minZoomPreference = 1f,
                maxZoomPreference = 47.5f,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true),
            onMapClick = { latLng ->
                viewModel.selectedLocation.value = latLng
                if (apiKey != null) {
                    viewModel.cleanNearbyPlaces()
                    termFilterList.forEach { filter ->
                        viewModel.fetchNearbyPlaces(latLng, apiKey, context, filter)
                    }
                }
            }
        ) {
            viewModel.selectedLocation.value?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Ubicación seleccionada",
                    snippet = "Lat: ${"%.4f".format(location.latitude)}, Lng: ${"%.4f".format(location.longitude)}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    onClick = {
                        val latitude = "%.4f".format(Locale.US, location.latitude).toFloat()
                        val longitude = "%.4f".format(Locale.US, location.longitude).toFloat()
                        navigator.navigate("add_camper_site/$latitude/$longitude")
                        true
                    }
                )
            }

            if (showNearbyPlaces) {
                nearbyPlaces.forEach { place ->
                    place.geometry?.location?.let { location ->
                        Marker(
                            state = MarkerState(position = LatLng(location.lat, location.lng)),
                            title = place.name,
                            snippet = place.vicinity,
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (place.placeId == selectedPlaceId) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                            ),
                            onClick = {
                                selectedPlaceId = place.placeId
                                viewModel.getPlaceDetailsFromPlaceId(place.placeId, apiKey ?: "", context)
                                true
                            }
                        )
                    }
                }
            }

            if (showFirebasePlaces) {
                firebaseCamperSites.forEach { site ->
                    Marker(
                        state = MarkerState(position = LatLng(site.location.latitude, site.location.longitude)),
                        title = site.name,
                        snippet = "Rating: ${site.rating} (${site.reviewCount} reviews)",
                        icon = BitmapDescriptorFactory.fromBitmap(
                            Bitmap.createScaledBitmap(
                                BitmapFactory.decodeResource(context.resources, R.drawable.camp_marker),
                                120, 120, false
                            )
                        ),
                        onClick = {
                            navigator.navigate("camper_site/${site.id}")
                            true
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            if (showNearbyPlaces) {
                viewModel.placeDetails.value?.let {
                    PlaceDetailsSection(place = it)
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(nearbyPlaces) { place ->
                        NearbyPlaceItem(place = place, onPlaceSelected = { selectedPlace ->
                            val latLng = selectedPlace.geometry?.location?.let {
                                LatLng(it.lat, it.lng)
                            }
                            viewModel.selectedLocation.value = latLng
                            latLng?.let { cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f)) }
                            selectedPlaceId = selectedPlace.placeId
                            viewModel.getPlaceDetailsFromPlaceId(selectedPlace.placeId, apiKey ?: "", context)
                        })
                    }
                }
            }

            SearchBarWithSuggestions(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                suggestions = searchSuggestions,
                onSuggestionSelected = {
                    onSearch(it.place_id)
                    onSearchQueryChange(it.description)
                },
                onCenterMap = centerMap,
                onSearch = handleSearch
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    // Perfil
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Surface(
                            color = Color.Transparent,
                            shape = CircleShape
                        ) {
                            IconButton(onClick = {
                                navigator.navigate("user_profile/${user?.email}")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Perfil"
                                )
                            }
                        }
                    }

                    // Mostrar campings de Firebase
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Surface(
                            color = if (showFirebasePlaces) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            shape = CircleShape
                        ) {
                            IconButton(onClick = {
                                showFirebasePlaces = !showFirebasePlaces
                                if (showFirebasePlaces) {
                                    val center = viewModel.selectedLocation.value ?: currentLocation ?: return@IconButton
                                    viewModel.fetchCamperSitesFromFirestore(
                                        center = center,
                                        radius = 10.0,
                                        context = context,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "${viewModel.firebaseCamperSites.size} sitios disponibles",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                } else {
                                    viewModel.clearFirebaseCamperSites()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Festival,
                                    contentDescription = if (showFirebasePlaces) "Ocultar campings" else "Mostrar campings"
                                )
                            }
                        }
                    }

                    // Mostrar lugares cercanos
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Surface(
                            color = if (showNearbyPlaces) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            shape = CircleShape
                        ) {
                            IconButton(onClick = {
                                if (termFilterList.isNotEmpty()) {
                                    showNearbyPlaces = !showNearbyPlaces
                                    if (apiKey != null && showNearbyPlaces) {
                                        viewModel.cleanNearbyPlaces()
                                        termFilterList.forEach { filter ->
                                            viewModel.fetchNearbyPlaces(viewModel.selectedLocation.value!!, apiKey, context, filter)
                                        }
                                    } else {
                                        viewModel.placeDetails.value = null
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.FindReplace,
                                    contentDescription = if (showNearbyPlaces) "Ocultar lugares" else "Mostrar lugares"
                                )
                            }
                        }
                    }

                    // Botón de filtros (sombreado no aplicado por estado específico aquí)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ToggleButtonGrid(onFilterSelected = handleFilterSelected)
                    }
                }
            }
        }
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
