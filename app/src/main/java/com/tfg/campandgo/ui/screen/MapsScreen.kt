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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.tfg.campandgo.data.model.Place
import com.tfg.campandgo.data.model.Prediction
import com.tfg.campandgo.ui.component.NearbyPlaceItem
import com.tfg.campandgo.ui.component.PlaceDetailsSection
import com.tfg.campandgo.ui.component.SearchBarWithSuggestions
import com.tfg.campandgo.ui.viewmodel.MapsViewModel

/**
 * Pantalla que muestra un mapa interactivo con funcionalidades de búsqueda y visualización de lugares cercanos.
 *
 * Esta pantalla incluye:
 * - Un mapa de Google Maps con marcadores para la ubicación actual y lugares cercanos.
 * - Una barra de búsqueda con sugerencias.
 * - Un botón para mostrar/ocultar la lista de lugares cercanos.
 * - Detalles del lugar seleccionado.
 *
 * @param currentLocation La ubicación actual del usuario.
 * @param searchQuery El texto de búsqueda ingresado por el usuario.
 * @param onSearchQueryChange Callback que se ejecuta cuando cambia el texto de búsqueda.
 * @param onSearch Callback que se ejecuta cuando se realiza una búsqueda.
 * @param searchSuggestions Lista de sugerencias de búsqueda.
 * @param errorMessage Mensaje de error a mostrar, si existe.
 * @param nearbyPlaces Lista de lugares cercanos.
 * @param viewModel El ViewModel que gestiona la lógica de la pantalla.
 *
 * @see MapsViewModel Para más detalles sobre el ViewModel utilizado.
 * @see SearchBarWithSuggestions Para la barra de búsqueda y sugerencias.
 * @see NearbyPlaceItem Para los elementos de la lista de lugares cercanos.
 */
@Composable
fun MapScreen(
    currentLocation: LatLng?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchSuggestions: List<Prediction>,
    nearbyPlaces: List<Place>,
    viewModel: MapsViewModel
) {
    val cameraPositionState = rememberCameraPositionState()
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }
    val context = LocalContext.current
    val apiKey = remember { getApiKeyFromManifest(context) }
    var showNearbyPlaces by remember { mutableStateOf(false) } // Estado para controlar la visibilidad
    var selectedPlaceId by remember { mutableStateOf<String?>(null) } // Estado para rastrear el lugar seleccionado
    var termFilterList by remember { mutableStateOf(listOf("")) } // Listado de terminos de filtrado

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
                    if (apiKey != null) viewModel.fetchNearbyPlaces(latLng, apiKey, context, termFilterList)
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }
        }
    }

    /**
     * Maneja los clics en el mapa.
     *
     * @param latLng La ubicación seleccionada en el mapa.
     */
    val handleMapClick: (LatLng) -> Unit = { latLng ->
        viewModel.selectedLocation.value = latLng
        if (apiKey != null) viewModel.fetchNearbyPlaces(viewModel.selectedLocation.value!!, apiKey, context, termFilterList)
    }

    // Actualizar la posición de la cámara cuando cambia la ubicación actual
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
            if (apiKey != null) viewModel.clearSearchLocations(context)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // GoogleMap con condicional para mostrar ubicaciones solo cuando showNearbyPlaces sea true
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true, // Esto habilita el punto azul de "Mi ubicación"
                minZoomPreference = 10f,
                maxZoomPreference = 20f,
                mapType = MapType.TERRAIN
            ),
            uiSettings = uiSettings,
            onMapClick = handleMapClick // Detectar el clic en el mapa
        ) {
            // Marcador de la ubicación seleccionada (solo si el usuario ha hecho clic)
            viewModel.selectedLocation.value?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Ubicación seleccionada",
                    snippet = "Lat: ${"%.4f".format(location.latitude)}, Lng: ${"%.4f".format(location.longitude)}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // Marcador azul
                )
            }

            // Solo mostrar lugares cercanos si showNearbyPlaces es true
            if (showNearbyPlaces) {
                nearbyPlaces.forEach { place ->
                    place.geometry?.location?.let { location ->
                        Marker(
                            state = MarkerState(position = LatLng(location.lat, location.lng)),
                            title = place.name,
                            snippet = place.vicinity,
                            icon = if (place.placeId == selectedPlaceId) {
                                // Marcador verde para el lugar seleccionado
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            } else {
                                // Marcador predeterminado para otros lugares
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            },
                            onClick = { marker ->
                                // Mostrar detalles del lugar seleccionado
                                selectedPlaceId = place.placeId // Actualizar el lugar seleccionado
                                val apiKey = getApiKeyFromManifest(context) ?: ""
                                viewModel.getPlaceDetailsFromPlaceId(place.placeId, apiKey, context)
                                true // Indica que el evento ha sido manejado
                            }
                        )
                    }
                }
            }
        }

        // Mostrar detalles del lugar seleccionado
        viewModel.placeDetails.value?.let { place ->
            PlaceDetailsSection(place = place)
        }

        // Botón para mostrar/ocultar la lista de lugares cercanos
        Button(
            onClick = {
                termFilterList = listOf("point_of_interest")
                if (apiKey != null) {
                    viewModel.fetchNearbyPlaces(viewModel.selectedLocation.value!!, apiKey, context, termFilterList)
                }
                showNearbyPlaces = !showNearbyPlaces // Cambia el estado
                if (!showNearbyPlaces) {
                    viewModel.placeDetails.value = null // Limpiar la información del lugar seleccionado
                }
            },
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
                        selectedPlaceId = selectedPlace.placeId // Actualizar el lugar seleccionado
                        val apiKey = getApiKeyFromManifest(context) ?: ""
                        viewModel.getPlaceDetailsFromPlaceId(selectedPlace.placeId, apiKey, context)
                    })
                }
            }
        }

        // Barra de búsqueda y sugerencias
        SearchBarWithSuggestions(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            suggestions = searchSuggestions,
            onSuggestionSelected = { prediction ->
                onSearch(prediction.place_id)
                onSearchQueryChange(prediction.description)
                viewModel.selectedLocation.value = null // Limpiar la ubicación seleccionada
                viewModel.placeDetails.value = null // Limpiar la información del lugar seleccionado
                showNearbyPlaces = false // Ocultar la lista de lugares cercanos
                selectedPlaceId = prediction.place_id // Actualizar el lugar seleccionado
            },
            onCenterMap = centerMap,
            onSearch = handleSearch
        )
    }
}

/**
 * Obtiene la API Key de Google Maps desde el archivo `AndroidManifest.xml`.
 *
 * @param context El contexto de la aplicación.
 * @return La API Key como String, o `null` si no se encuentra o hay un error.
 */
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