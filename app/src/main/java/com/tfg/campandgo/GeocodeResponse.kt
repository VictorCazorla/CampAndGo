package com.tfg.campandgo

data class GeocodeResponse(
    val results: List<GeocodeResult>,
    val status: String
)

data class GeocodeResult(
    val geometry: Geometry,
    val placeId: String
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)
