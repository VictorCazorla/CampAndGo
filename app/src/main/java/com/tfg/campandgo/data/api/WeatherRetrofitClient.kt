package com.tfg.campandgo.data.api

/**
 * Servicio de la API de OpenWeather.
 * Se inicializa de forma perezosa usando la instancia única de RetrofitFactory.
 */
object WeatherRetrofitClient {

    private const val BASE_URL = "https://api.openweathermap.org/"

    val weatherService: WeatherService by lazy {
        RetrofitFactory.create(BASE_URL).create(WeatherService::class.java)
    }
}
