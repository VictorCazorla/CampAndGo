package com.tfg.campandgo

import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

class MapsViewModel : ViewModel() {
    var hasLocationPermission = mutableStateOf(false)
    var searchSuggestions = mutableStateListOf<Prediction>()
    var selectedLocation = mutableStateOf<LatLng?>(null)
    var errorMessage = mutableStateOf<String?>(null)

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
}