package com.tfg.campandgo.ui.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
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
    var nearbyPlaces = mutableStateListOf<Place>()

    /**
     * Busca ubicaciones utilizando la API de autocompletado de Google Places.
     *
     * @param query El texto de búsqueda.
     * @param apiKey La API Key de Google Places.
     * @param context El contexto de la aplicación para mostrar Toasts.
     */
    fun searchLocations(query: String, apiKey: String, context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.placesService.autocomplete(
                    input = query,
                    key = apiKey
                )

                if (response.status == "OK") {
                    searchSuggestions.clear()
                    searchSuggestions.addAll(response.predictions)
                } else {
                    Toast.makeText(context, "No se encontraron sugerencias.", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error buscando ubicaciones: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Limpia las ubicaciones de la API de autocompletado de Google Places.
     *
     * @param context El contexto de la aplicación para mostrar Toasts.
     */
    fun clearSearchLocations(context: Context) {
        viewModelScope.launch {
            try {
                searchSuggestions.clear()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error cerrando ubicaciones: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Obtiene las coordenadas de una ubicación utilizando su placeId.
     *
     * @param placeId El ID del lugar.
     * @param apiKey La API Key de Google Places.
     * @param context El contexto de la aplicación para mostrar Toasts.
     */
    fun getLocationDetails(placeId: String, apiKey: String, context: Context) {
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
                } else {
                    Toast.makeText(context, "Error obteniendo coordenadas.", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error obteniendo coordenadas: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                Toast.makeText(context, "No se encontró la dirección.", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Error geocodificando la dirección: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            onResult(null)
        }
    }

    /**
     * Obtiene los detalles de un lugar utilizando su placeId.
     *
     * @param placeId El ID del lugar.
     * @param apiKey La API Key de Google Places.
     * @param context El contexto de la aplicación para mostrar Toasts.
     */
    fun getPlaceDetailsFromPlaceId(placeId: String, apiKey: String, context: Context) {
        viewModelScope.launch {
            try {
                if (placeId.isNotEmpty()) {
                    Log.e("MapsViewModel", "placeId: $placeId - apiKey: $apiKey")

                    val response = RetrofitClient.placesService.getPlaceDetails(placeId, apiKey)
                    if (response.status == "OK") {
                        placeDetails.value = response.result
                    } else {
                        Toast.makeText(
                            context,
                            "Error obteniendo detalles del lugar: ${response.status}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "MapsViewModel",
                            "Error obteniendo detalles del lugar: ${response.status} - ${response.result}"
                        )
                    }
                } else {
                    Toast.makeText(context, "placeId inválido: $placeId", Toast.LENGTH_SHORT).show()
                    Log.e("MapsViewModel", "placeId inválido: $placeId")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error en la solicitud: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("MapsViewModel", "Excepción en getPlaceDetails: ${e.message}")
            }
        }
    }

    /**
     * Busca lugares cercanos (restaurantes, cafés, etc.) utilizando la API de Google Places.
     *
     * @param location La ubicación desde la cual buscar.
     * @param apiKey La API Key de Google Places.
     * @param context El contexto de la aplicación para mostrar Toasts.
     * @param terms La lista de términos para filtrar los lugares.
     */
    fun fetchNearbyPlaces(
        location: LatLng,
        apiKey: String,
        context: Context,
        type: String
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.placesService.nearbySearch(
                    location = "${location.latitude},${location.longitude}",
                    radius = 1000,
                    type = type,
                    key = apiKey
                )

                if (response.status == "OK") {
                    nearbyPlaces.addAll(response.results)
                    nearbyPlaces.forEach { place -> Log.d("MapsViewModel", "Places: $place") }
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Limpia todos los lugares cercanos
     */
    fun cleanNearbyPlaces() {
        viewModelScope.launch {
            nearbyPlaces.clear()
        }
    }
}
