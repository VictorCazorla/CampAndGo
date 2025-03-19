package com.tfg.campandgo.data.model

/**
 * Clase de datos que representa la respuesta de los detalles de un lugar.
 *
 * @property result Un objeto de tipo `PlaceDetails` que contiene la información detallada del lugar.
 * @property status Un String que representa el estado de la respuesta (por ejemplo, "OK" o "ERROR").
 */
data class PlaceDetailsResponse(
    val result: PlaceDetails?,
    val status: String
)

/**
 * Clase de datos que contiene los detalles específicos de un lugar.
 *
 * @property name El nombre del lugar (opcional).
 * @property formatted_address La dirección formateada del lugar (opcional).
 * @property types Una lista de tipos categóricos asociados al lugar (opcional).
 */
data class PlaceDetails(
    val name: String?,
    val formatted_address: String?,
    val types: List<String>?
)
