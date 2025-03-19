package com.tfg.campandgo.ui.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.tfg.campandgo.data.api.RetrofitClient
import com.tfg.campandgo.data.model.Place
import com.tfg.campandgo.data.model.PlaceDetails
import com.tfg.campandgo.data.model.Prediction
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

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
    var errorMessage = mutableStateOf<String?>(null)
    var nearbyPlaces = mutableStateListOf<Place>()

    /**
     * Busca ubicaciones utilizando la API de autocompletado de Google Places.
     *
     * @param query El texto de búsqueda.
     * @param apiKey La API Key de Google Places.
     */
    fun searchLocations(query: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.placesService.autocomplete(
                    input = query,
                    key = apiKey
                )

                if (response.status == "OK") {
                    searchSuggestions.clear()
                    searchSuggestions.addAll(response.predictions)
                }
            } catch (e: Exception) {
                errorMessage.value = "Error buscando ubicaciones: ${e.message}"
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
                val response = RetrofitClient.placesService.geocode(
                    address = placeId,
                    key = apiKey
                )

                if (response.status == "OK") {
                    response.results.firstOrNull()?.geometry?.location?.let {
                        selectedLocation.value = LatLng(it.lat, it.lng)
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = "Error obteniendo coordenadas: ${e.message}"
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
                    Log.e("MapsViewModel", "placeId: $placeId - apiKey: $apiKey")

                    val response = RetrofitClient.placesService.getPlaceDetails(placeId, apiKey)
                    if (response.status == "OK") {
                        placeDetails.value = response.result
                    } else {
                        errorMessage.value = "Error obteniendo detalles del lugar: ${response.status}"
                        Log.e("MapsViewModel", "Error obteniendo detalles del lugar: ${response.status} - ${response.result}")
                    }
                } else {
                    errorMessage.value = "placeId inválido: $placeId"
                    Log.e("MapsViewModel", "placeId inválido: $placeId")
                }
            } catch (e: Exception) {
                errorMessage.value = "Error en la solicitud: ${e.message}"
                Log.e("MapsViewModel", "Excepción en getPlaceDetails: ${e.message}")
            }
        }
    }

    /**
     * Busca lugares cercanos (restaurantes, cafés, etc.) utilizando la API de Google Places.
     *
     * @param location La ubicación desde la cual buscar.
     * @param apiKey La API Key de Google Places.
     */
    fun fetchNearbyPlaces(location: LatLng, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.placesService.nearbySearch(
                    location = "${location.latitude},${location.longitude}",
                    radius = 1000,
                    type = "restaurant",
                    key = apiKey
                )

                if (response.status == "OK") {
                    nearbyPlaces.clear()
                    nearbyPlaces.addAll(response.results)
                } else {
                    errorMessage.value = "No se encontraron lugares cercanos."
                }
            } catch (e: Exception) {
                errorMessage.value = "Error buscando lugares cercanos: ${e.message}"
            }
        }
    }
}