package com.tfg.campandgo.data.model

data class PlaceDetailsResponse(
    val result: PlaceDetails?,
    val status: String
)

data class PlaceDetails(
    val name: String?,
    val formatted_address: String?,
    val types: List<String>?
)