package com.tfg.campandgo.data.model

/**
 * Clase de datos que representa la respuesta de una solicitud del tiempo.
 *
 * @property weather Clase con la descripción del tiempo.
 * @property main Clase con la respuesta principal con la temperatura y la humedad.
 * @property name Nombre.
 */
data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val name: String
)

/**
 * Clase de datos que representa la descripción del tiempo.
 *
 * @property description La descripción del tiempo.
 * @property icon Un icono.
 */
data class Weather(
    val description: String,
    val icon: String
)

/**
 * Clase de datos que representa la respuesta principal de la solicitud del tiempo.
 *
 * @property temp La temperatura.
 * @property humidity La humedad.
 */
data class Main(
    val temp: Double,
    val humidity: Int
)
