package com.tfg.campandgo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodeService {
    @GET("maps/api/geocode/json")
    fun getGeocodeDetails(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): Call<GeocodeResponse>

    @GET("maps/api/place/details/json")
    fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): Call<PlaceDetailsResponse>
}