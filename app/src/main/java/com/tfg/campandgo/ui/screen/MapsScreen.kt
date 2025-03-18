package com.tfg.campandgo.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.tfg.campandgo.data.model.Place
import com.tfg.campandgo.data.model.Prediction
import com.tfg.campandgo.ui.components.NearbyPlaceItem
import com.tfg.campandgo.ui.components.SearchBarWithSuggestions
import com.tfg.campandgo.ui.viewmodel.MapsViewModel

@Composable
fun MapScreen(
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
    val apiKey = remember { getApiKeyFromManifest(context) }
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
                    viewModel.selectedLocation.value = it
                    if (apiKey != null) viewModel.fetchNearbyPlaces(latLng, apiKey)
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
        if (apiKey != null) viewModel.fetchNearbyPlaces(viewModel.selectedLocation.value!!, apiKey)
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