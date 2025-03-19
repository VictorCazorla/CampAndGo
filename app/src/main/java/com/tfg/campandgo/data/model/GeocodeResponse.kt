package com.tfg.campandgo.data.model

/**
 * Clase de datos que representa la respuesta de una solicitud de geocodificación.
 *
 * @property results Una lista de objetos `GeocodeResult` que contienen los resultados de la geocodificación.
 * @property status Un String que indica el estado de la respuesta (por ejemplo, "OK" o "ERROR").
 */
data class GeocodeResponse(
    val results: List<GeocodeResult>,
    val status: String
)

/**
 * Clase de datos que representa un resultado individual de la geocodificación.
 *
 * @property geometry Un objeto `Geometry` que contiene la información geométrica del resultado.
 * @property placeId Un identificador único para el lugar asociado al resultado.
 */
data class GeocodeResult(
    val geometry: Geometry,
    val placeId: String
)

/**
 * Clase de datos que contiene la información geométrica de un resultado.
 *
 * @property location Un objeto `Location` que representa la ubicación geográfica del lugar.
 */
data class Geometry(
    val location: Location
)

/**
 * Clase de datos que representa una ubicación geográfica.
 *
 * @property lat La latitud de la ubicación.
 * @property lng La longitud de la ubicación.
 */
data class Location(
    val lat: Double,
    val lng: Double
)
