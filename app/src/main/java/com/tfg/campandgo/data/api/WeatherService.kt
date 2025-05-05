package com.tfg.campandgo.data.api

import com.tfg.campandgo.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz que define los servicios de la API de OpenWeather.
 * Contiene la solicitud del tiempo utilizando Retrofit.
 */
interface WeatherService {

    /**
     * Solicitud para obtener el tiempo basado en una localización.
     *
     * @param lat Latitud.
     * @param lon Longitud.
     * @param appid La API key de OpenWeather.
     * @return Una respuesta `WeatherResponse` con el tiempo.
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): WeatherResponse
}
