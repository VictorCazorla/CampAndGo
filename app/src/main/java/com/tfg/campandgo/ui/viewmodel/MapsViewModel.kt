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
import com.tfg.campandgo.data.model.GeocodeResponse
import com.tfg.campandgo.data.model.GeocodeResult
import com.tfg.campandgo.data.model.Place
import com.tfg.campandgo.data.model.PlaceDetails
import com.tfg.campandgo.data.model.Prediction
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel : ViewModel() {
    var hasLocationPermission = mutableStateOf(false)
    var searchSuggestions = mutableStateListOf<Prediction>()
    var selectedLocation = mutableStateOf<LatLng?>(null)
    var placeDetails = mutableStateOf<PlaceDetails?>(null)
    var errorMessage = mutableStateOf<String?>(null)
    var nearbyPlaces = mutableStateListOf<Place>()

    /**
     * Función para buscar ubicaciones con autocomplete
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
     * Función para obtener detalles de una ubicación utilizando el placeId
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
     * Función para geocodificar una dirección a LatLng usando la API de Google
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

    fun geocodeResultAddress(address: String, context: Context, onResult: (GeocodeResult?) -> Unit) {
        val apiKey = getApiKeyFromManifest(context) ?: ""
        val call = RetrofitClient.geocodeService.getGeocodeDetails(address, apiKey)

        call.enqueue(object : Callback<GeocodeResponse> {
            override fun onResponse(call: Call<GeocodeResponse>, response: Response<GeocodeResponse>) {
                if (response.isSuccessful) {
                    val geocodeResponse = response.body()

                    if (geocodeResponse?.results?.isNotEmpty() == true) {
                        onResult(geocodeResponse.results[0])    // Retorna el primer resultado
                    } else {
                        onResult(null)
                    }
                } else {
                    Toast.makeText(context, "Error en la geocodificación: ${response.message()}", Toast.LENGTH_SHORT).show()
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<GeocodeResponse>, t: Throwable) {
                Toast.makeText(context, "Error en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
        })
    }

    fun getApiKeyFromManifest(context: Context): String? {
        val applicationInfo = context.applicationInfo
        val metaData = applicationInfo.metaData
        return metaData?.getString("com.google.android.geo.API_KEY")
    }

    /**
     * Función para obtener detalles del lugar usando el placeId
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
     * Función para buscar lugares cercanos (restaurantes, cafés, etc.)
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