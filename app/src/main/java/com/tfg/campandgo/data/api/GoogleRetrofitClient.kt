package com.tfg.campandgo.data.api

import com.tfg.campandgo.data.api.service.GooglePlacesService

/**
 * Servicio de la API de Google Places.
 * Se inicializa de forma perezosa usando la instancia única de RetrofitFactory.
 */
object GoogleRetrofitClient {

    private const val BASE_URL = "https://maps.googleapis.com/"

    val placesService: GooglePlacesService by lazy {
        RetrofitFactory.create(BASE_URL).create(GooglePlacesService::class.java)
    }
}
