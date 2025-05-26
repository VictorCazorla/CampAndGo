package com.tfg.campandgo.ui.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.campandgo.data.api.GoogleRetrofitClient
import com.tfg.campandgo.data.model.CamperSite
import com.tfg.campandgo.data.model.Place
import com.tfg.campandgo.data.model.PlaceDetails
import com.tfg.campandgo.data.model.Prediction
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * ViewModel para gestionar la lógica relacionada con el mapa y las ubicaciones.
 *
 * Este ViewModel se encarga de:
 * - Gestionar los permisos de ubicación.
 * - Realizar búsquedas de ubicaciones y sugerencias.
 * - Obtener detalles de lugares.
 * - Buscar lugares cercanos.
 * - Geocodificar direcciones a coordenadas.
 *
 */
class MapsViewModel : ViewModel() {
    var hasLocationPermission = mutableStateOf(false)
    var searchSuggestions = mutableStateListOf<Prediction>()
    var selectedLocation = mutableStateOf<LatLng?>(null)
    var placeDetails = mutableStateOf<PlaceDetails?>(null)
    var nearbyPlaces = mutableStateListOf<Place>()
    var firebaseCamperSites = mutableStateListOf<CamperSite>()
    var selectedAmenities = mutableStateOf(setOf<String>())
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Busca ubicaciones utilizando la API de autocompletado de Google Places.
     *
     * @param query El texto de búsqueda.
     * @param apiKey La API Key de Google Places.
     */
    fun searchLocations(query: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = GoogleRetrofitClient.placesService.autocomplete(
                    input = query,
                    key = apiKey
                )

                if (response.status == "OK") {
                    searchSuggestions.clear()
                    searchSuggestions.addAll(response.predictions)
                }
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error searching for locations: ${e.message}")
            }
        }
    }

    /**
     * Limpia las ubicaciones de la API de autocompletado de Google Places.
     */
    fun clearSearchLocations() {
        viewModelScope.launch {
            try {
                searchSuggestions.clear()
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error closing locations: ${e.message}")
            }
        }
    }

    /**
     * Obtiene las coordenadas de una ubicación utilizando su placeId.
     *
     * @param placeId El ID del lugar.
     * @param apiKey La API Key de Google Places.
     */
    fun getLocationDetails(placeId: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = GoogleRetrofitClient.placesService.geocode(
                    address = placeId,
                    key = apiKey
                )

                if (response.status == "OK") {
                    response.results.firstOrNull()?.geometry?.location?.let {
                        selectedLocation.value = LatLng(it.lat, it.lng)
                    }
                }
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error: ${e.message}")
            }
        }
    }

    /**
     * Geocodifica una dirección a coordenadas (LatLng) utilizando el Geocoder de Android.
     *
     * @param address La dirección a geocodificar.
     * @param context El contexto de la aplicación.
     * @param onResult Callback que se ejecuta con el resultado de la geocodificación.
     */
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
            Log.e("MapsViewModel", "Error geocoding the address: ${e.message}")
            onResult(null)
        }
    }

    /**
     * Obtiene los detalles de un lugar utilizando su placeId.
     *
     * @param placeId El ID del lugar.
     * @param apiKey La API Key de Google Places.
     */
    fun getPlaceDetailsFromPlaceId(placeId: String, apiKey: String) {
        viewModelScope.launch {
            try {
                if (placeId.isNotEmpty()) {
                    val response = GoogleRetrofitClient.placesService.getPlaceDetails(placeId, apiKey)
                    if (response.status == "OK") {
                        placeDetails.value = response.result
                    } else {
                        Log.e("MapsViewModel", "Error getting location details: ${response.status}")
                    }
                } else {
                    Log.e("MapsViewModel", "Invalid camper site: $placeId")
                }
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Exception in getPlaceDetails: ${e.message}")
            }
        }
    }

    /**
     * Busca lugares cercanos (restaurantes, cafés, etc.) utilizando la API de Google Places.
     *
     * @param location La ubicación desde la cual buscar.
     * @param apiKey La API Key de Google Places.
     * @param type El término para filtrar los lugares.
     */
    fun fetchNearbyPlaces(
        location: LatLng,
        apiKey: String,
        type: String
    ) {
        viewModelScope.launch {
            try {
                val response = GoogleRetrofitClient.placesService.nearbySearch(
                    location = "${location.latitude},${location.longitude}",
                    radius = 1000,
                    type = type,
                    key = apiKey
                )

                if (response.status == "OK") {
                    nearbyPlaces.addAll(response.results)
                }
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error: ${e.message}")
            }
        }
    }

    /**
     * Limpia todos los lugares cercanos.
     */
    fun cleanNearbyPlaces() {
        viewModelScope.launch {
            nearbyPlaces.clear()
        }
    }

    /**
     * Busca sitios camper cercanos almacenados en Firestore.
     *
     * @param center La ubicación desde la cual buscar.
     * @param radius El radio de búesqueda.
     */
    fun fetchCamperSitesFromFirestore(
        center: LatLng,
        radius: Double,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                firestore.collection("camper_sites")
                    .get()
                    .addOnSuccessListener { result ->
                        val sites = result.documents.mapNotNull { doc ->
                            val geoPoint = doc.getGeoPoint("location") ?: return@mapNotNull null
                            val siteLocation = LatLng(geoPoint.latitude, geoPoint.longitude)

                            // Calcular distancia
                            val distance = calculateHaversineDistance(
                                center.latitude,
                                center.longitude,
                                siteLocation.latitude,
                                siteLocation.longitude
                            )

                            if (distance <= radius) {
                                CamperSite(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "",
                                    formattedAddress = doc.getString("formattedAddress") ?: "",
                                    description = doc.getString("description") ?: "",
                                    mainImageUrl = doc.getString("mainImageUrl") ?: "",
                                    images = doc.get("images") as? List<String> ?: emptyList(),
                                    rating = doc.getDouble("rating") ?: 0.0,
                                    reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                                    amenities = doc.get("amenities") as? List<String> ?: emptyList(),
                                    location = geoPoint
                                )
                            } else {
                                null
                            }
                        }

                        firebaseCamperSites.clear()
                        firebaseCamperSites.addAll(sites)
                        onSuccess?.invoke()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MapsViewModel", "Error: ${exception.message}")
                    }
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error: ${e.message}")
            }
        }
    }

    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula Haversine.
     *
     * @param lat1 Latitud del primer punto.
     * @param lon1 Longitud del primer punto.
     * @param lat2 Latitud del segundo punto.
     * @param lon2 Longitud del segundo punto.
     * @return Distancia en kilómetros.
     */
    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    /**
     * Limpia los sitios camper cargados desde Firebase.
     */
    fun clearFirebaseCamperSites() {
        viewModelScope.launch {
            firebaseCamperSites.clear()
        }
    }

    /**
     * Añade y quita los filtros.
     *
     * @param amenity El filtro.
     */
    fun toggleAmenityFilter(amenity: String) {
        selectedAmenities.value = if (selectedAmenities.value.contains(amenity)) {
            selectedAmenities.value - amenity
        } else {
            selectedAmenities.value + amenity
        }
    }
}
