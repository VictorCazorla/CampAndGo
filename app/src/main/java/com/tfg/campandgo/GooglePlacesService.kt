package com.tfg.campandgo

import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesService {
    @GET("maps/api/place/autocomplete/json")
    suspend fun autocomplete(
        @Query("input") input: String,
        @Query("key") key: String,
        @Query("language") language: String = "es",
        @Query("components") components: String = "country:es"
    ): AutocompleteResponse

    @GET("maps/api/geocode/json")
    suspend fun geocode(
        @Query("address") address: String,
        @Query("key") key: String
    ): GeocodeResponse

    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): PlaceDetailsResponse
}