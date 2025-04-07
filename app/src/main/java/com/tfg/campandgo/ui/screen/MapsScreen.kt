package com.tfg.campandgo.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import com.tfg.campandgo.data.model.CamperSite
import com.tfg.campandgo.data.model.CamperSiteReview
import com.tfg.campandgo.data.model.Place
import com.tfg.campandgo.data.model.Prediction
import com.tfg.campandgo.ui.component.NearbyPlaceItem
import com.tfg.campandgo.ui.component.PlaceDetailsSection
import com.tfg.campandgo.ui.component.SearchBarWithSuggestions
import com.tfg.campandgo.ui.component.ToggleButtonGrid
import com.tfg.campandgo.ui.viewmodel.MapsViewModel
import kotlinx.coroutines.tasks.await

/**
 * Pantalla que muestra un mapa interactivo con funcionalidades de búsqueda y visualización de lugares cercanos.
 *
 * Esta pantalla incluye:
 * - Un mapa de Google Maps con marcadores para la ubicación actual y lugares cercanos.
 * - Una barra de búsqueda con sugerencias.
 * - Un botón para mostrar/ocultar la lista de lugares cercanos.
 * - Detalles del lugar seleccionado.
 * - Un grid de botones para filtrar lugares cercanos.
 *
 * @param currentLocation La ubicación actual del usuario.
 * @param searchQuery El texto de búsqueda ingresado por el usuario.
 * @param onSearchQueryChange Callback que se ejecuta cuando cambia el texto de búsqueda.
 * @param onSearch Callback que se ejecuta cuando se realiza una búsqueda.
 * @param searchSuggestions Lista de sugerencias de búsqueda.
 * @param nearbyPlaces Lista de lugares cercanos.
 * @param viewModel El ViewModel que gestiona la lógica de la pantalla.
 */
@SuppressLint("MutableCollectionMutableState")
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
    var showNearbyPlaces by remember { mutableStateOf(false) }
    var selectedPlaceId by remember { mutableStateOf<String?>(null) }
    var termFilterList by remember { mutableStateOf(mutableListOf<String>()) }

    // Mover la cámara a la ubicación actual al inicio
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location, 15f))
            if (apiKey != null) viewModel.clearSearchLocations(context)
        }
    }

    val handleFilterSelected: (List<String>) -> Unit = { filters ->
        termFilterList = filters.toMutableList()
        Log.d("MapsViewModel", "Filtros seleccionados: $termFilterList")

        if (termFilterList.isEmpty()) {
            viewModel.cleanNearbyPlaces()
            showNearbyPlaces = false
            viewModel.placeDetails.value = null
        } else if (apiKey != null && showNearbyPlaces) {
            viewModel.cleanNearbyPlaces()
            for (filter in termFilterList) {
                viewModel.fetchNearbyPlaces(
                    viewModel.selectedLocation.value!!,
                    apiKey,
                    context,
                    filter
                )
            }
        }
    }

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
                    if (apiKey != null) {
                        viewModel.cleanNearbyPlaces()
                        for (filter in termFilterList) {
                            viewModel.fetchNearbyPlaces(
                                viewModel.selectedLocation.value!!,
                                apiKey,
                                context,
                                filter
                            )
                        }
                    }
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.fillMaxSize().weight(0.7f), contentAlignment = Alignment.Center) {
            // GoogleMap con condicional para mostrar ubicaciones solo cuando showNearbyPlaces sea true
            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    minZoomPreference = 1f,
                    maxZoomPreference = 50f,
                    mapType = MapType.NORMAL
                ),
                uiSettings = uiSettings,
                onMapClick = { latLng ->
                    viewModel.selectedLocation.value = latLng
                    if (apiKey != null) {
                        viewModel.cleanNearbyPlaces()
                        for (filter in termFilterList) {
                            viewModel.fetchNearbyPlaces(
                                viewModel.selectedLocation.value!!,
                                apiKey,
                                context,
                                filter
                            )
                        }
                    }
                }
            ) {
                // Marcador de la ubicación seleccionada
                viewModel.selectedLocation.value?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Ubicación seleccionada",
                        snippet = "Lat: ${"%.4f".format(location.latitude)}, Lng: ${
                            "%.4f".format(
                                location.longitude
                            )
                        }",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // Mostrar lugares cercanos si showNearbyPlaces es true
                if (showNearbyPlaces) {
                    nearbyPlaces.forEach { place ->
                        place.geometry?.location?.let { location ->
                            // Verificar si el lugar tiene alguno de los tipos que estamos buscando
                            val hasMatchingType = place.types?.all { type ->
                                type.equals("campground", ignoreCase = true) ||
                                        type.equals("camping", ignoreCase = true) ||
                                        type.equals("rv_park", ignoreCase = true) ||
                                        type.equals("park", ignoreCase = true)
                            } == true

                            Marker(
                                state = MarkerState(position = LatLng(location.lat, location.lng)),
                                title = place.name,
                                snippet = place.vicinity,
                                icon = if (hasMatchingType) {
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
                                } else if (place.placeId == selectedPlaceId) {
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                } else {
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                },
                                onClick = { marker ->
                                    selectedPlaceId = place.placeId
                                    val apiKey = getApiKeyFromManifest(context) ?: ""
                                    viewModel.getPlaceDetailsFromPlaceId(
                                        place.placeId,
                                        apiKey,
                                        context
                                    )
                                    true
                                }
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize().align(
                Alignment.TopCenter
            )) {
                ToggleButtonGrid(onFilterSelected = handleFilterSelected,)
            }
        }

        Column() {
            // Mostrar detalles del lugar seleccionado
            viewModel.placeDetails.value?.let { place ->
                PlaceDetailsSection(place = place)
            }

            // Botón para mostrar/ocultar la lista de lugares cercanos
            Button(
                onClick = {
                    if (termFilterList.isNotEmpty()) {
                        if (apiKey != null) {
                            viewModel.cleanNearbyPlaces()
                            for (filter in termFilterList) {
                                viewModel.fetchNearbyPlaces(
                                    viewModel.selectedLocation.value!!,
                                    apiKey,
                                    context,
                                    filter
                                )
                            }
                        }
                        showNearbyPlaces = !showNearbyPlaces
                        if (!showNearbyPlaces) {
                            viewModel.placeDetails.value = null
                        }
                    } else {
                        Toast.makeText(context, "Selecciona al menos un filtro primero", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),

                enabled = termFilterList.isNotEmpty()
            ) {
                Text(if (showNearbyPlaces) "Ocultar lugares cercanos" else "Mostrar lugares cercanos")
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
                    viewModel.placeDetails.value =
                        null // Limpiar la información del lugar seleccionado
                    showNearbyPlaces = false // Ocultar la lista de lugares cercanos
                    selectedPlaceId = prediction.place_id // Actualizar el lugar seleccionado
                },
                onCenterMap = centerMap,
                onSearch = handleSearch
            )
        }

        // Lista de lugares cercanos (solo si showNearbyPlaces es true)
        if (showNearbyPlaces) {
            LazyColumn(modifier = Modifier.weight(0.3f).padding(8.dp)) {
                items(nearbyPlaces) { place ->
                    NearbyPlaceItem(place = place, onPlaceSelected = { selectedPlace ->
                        val latLng = selectedPlace.geometry?.location?.let {
                            LatLng(
                                it.lat,
                                selectedPlace.geometry.location.lng
                            )
                        }

                        // Actualizar la ubicación seleccionada en ViewModel
                        viewModel.selectedLocation.value = latLng

                        // Centrar el mapa en la ubicación del lugar seleccionado
                        latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
                            ?.let { cameraPositionState.move(it) }

                        // Obtener detalles del lugar seleccionado
                        selectedPlaceId =
                            selectedPlace.placeId // Actualizar el lugar seleccionado
                        val apiKey = getApiKeyFromManifest(context) ?: ""
                        viewModel.getPlaceDetailsFromPlaceId(
                            selectedPlace.placeId,
                            apiKey,
                            context
                        )
                    })
                }
            }
        }
    }

    launchCampsite()
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

@Composable
private fun launchCampsite() {

    var sampleCamperSite by remember { mutableStateOf(
        CamperSite(
            "", "", "", "", "", listOf(""), 0.0, 0, listOf(""), listOf(CamperSiteReview("", 0.0, "", "", listOf())))
    ) }
    val db = Firebase.firestore

    // Versión 1
    /*db.collection("camper_sites")
        .get()
        .addOnSuccessListener { camperSites ->
            if (camperSites.isEmpty) {
                Log.d("LaunchCampsite", "No camper site found")
            }

            for (camperSite in camperSites) {
                sampleCamperSite = CamperSite(
                    id = camperSite.data["id"].toString(),
                    name = camperSite.data["name"].toString(),
                    formattedAddress = camperSite.data["formatted_address"].toString(),
                    description = camperSite.data["description"].toString(),
                    mainImageUrl = camperSite.data["main_image_url"].toString(),
                    images = (camperSite["images"] as List<*>).map { it as String },
                    rating = camperSite.data["rating"].toString().toDouble(),
                    reviewCount = camperSite.data["review_count"].toString().toInt(),
                    amenities = (camperSite["amenities"] as List<*>).map { it as String },
                    reviews = (camperSite["reviews"] as List<*>).map { it as CamperSiteReview }
                )
            }
        }
        .addOnFailureListener { e ->
            Log.d("MapsViewModel", "Error searching the camper site", e)
        }*/
    // Usar un LaunchedEffect para realizar la consulta solo una vez
    LaunchedEffect(Unit) {
        // Versión 2
        /*db.collection("camper_sites")
            .whereEqualTo("id", "long_lat")
            .get()
            .addOnSuccessListener { camperSites ->
                if (camperSites.isEmpty) {
                    Log.d("MapsViewModel", "No camper site found")
                    return@addOnSuccessListener
                }

                val camperSite = camperSites.first() // Asumiendo que hay al menos un resultado
                sampleCamperSite = CamperSite(
                    id = camperSite.data["id"].toString(),
                    name = camperSite.data["name"].toString(),
                    formattedAddress = camperSite.data["formatted_address"].toString(),
                    description = camperSite.data["description"].toString(),
                    mainImageUrl = camperSite.data["main_image_url"].toString(),
                    images = (camperSite["images"] as List<*>).map { it as String },
                    rating = camperSite.data["rating"].toString().toDouble(),
                    reviewCount = camperSite.data["review_count"].toString().toInt(),
                    amenities = (camperSite["amenities"] as List<*>).map { it as String },
                    reviews = (camperSite["reviews"] as List<*>).map { it as CamperSiteReview }
                )
            }
            .addOnFailureListener { e ->
                Log.d("MapsViewModel", "Error searching the camper site", e)
            }*/

        // Versión 3
        try {
            val camperSitesSnapshot = db.collection("camper_sites")
                .get()
                .await()  // Espera respuesta de manera síncrona

            Log.d("LaunchCampsite", "Camper site: $camperSitesSnapshot")

            if (camperSitesSnapshot.isEmpty) {
                Log.d("LaunchCampsite", "No camper site found")
            } else {
                Log.d("LaunchCampsite", "Camper site: $sampleCamperSite")
                val camperSite = camperSitesSnapshot.documents.first()
                Log.d("LaunchCampsite", "Camper site: $camperSite")
                sampleCamperSite = CamperSite(
                    id = camperSite.getString("id") ?: "",
                    name = camperSite.getString("name") ?: "",
                    formattedAddress = camperSite.getString("formatted_address") ?: "",
                    description = camperSite.getString("description") ?: "",
                    mainImageUrl = camperSite.getString("main_image_url") ?: "",
                    images = camperSite.get("images") as? List<String> ?: listOf(),
                    rating = camperSite.getDouble("rating") ?: 0.0,
                    reviewCount = camperSite.getLong("review_count")?.toInt() ?: 0,
                    amenities = camperSite.get("amenities") as? List<String> ?: listOf(),
                    reviews = (camperSite.get("reviews") as? List<Map<String, Any>>)?.map {
                        CamperSiteReview(
                            userName = it["user_name"] as? String ?: "",
                            rating = (it["rating"] as? Double) ?: 0.0,
                            comment = it["comment"] as? String ?: "",
                            date = it["date"] as? String ?: "",
                            images = it["images"] as? List<String> ?: listOf()
                        )
                    } ?: listOf()
                )
                Log.d("LaunchCampsite", "Camper site: $sampleCamperSite")
            }
        } catch (e: Exception) {
            Log.e("LaunchCampsite", "Error fetching camper site", e)
        }
    }

    CamperSiteScreen(
        site = sampleCamperSite,
        onBackClick = { /* Lógica para volver atrás */ },
        onBookClick = { /* Lógica para reservar */ }
    )
}