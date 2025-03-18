package com.tfg.campandgo.data.model

import com.google.gson.annotations.SerializedName

data class NearbySearchResponse(
    @SerializedName("results") val results: List<Place>,
    @SerializedName("status") val status: String
)

data class Place(
    @SerializedName("name") val name: String,
    @SerializedName("vicinity") val vicinity: String?,
    @SerializedName("geometry") val geometry: GeometryN?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("place_id") val placeId: String
)

data class GeometryN(
    @SerializedName("location") val location: LocationN
)

data class LocationN(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)