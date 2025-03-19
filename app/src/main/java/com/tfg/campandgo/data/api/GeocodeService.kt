package com.tfg.campandgo.data.api

import com.tfg.campandgo.data.model.GeocodeResponse
import com.tfg.campandgo.data.model.PlaceDetailsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz que define los servicios relacionados con la geocodificación y los detalles de lugares
 * utilizando Retrofit para interactuar con la API de Google Maps.
 */
interface GeocodeService {

    /**
     * Solicitud para obtener los detalles geográficos (geocodificación) de una dirección.
     *
     * @param address La dirección que se desea geocodificar.
     * @param apiKey La clave de API de Google necesaria para autenticar la solicitud.
     * @return Un objeto `Call` que contiene la respuesta `GeocodeResponse` con los detalles de la geocodificación.
     */
    @GET("maps/api/geocode/json")
    fun getGeocodeDetails(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): Call<GeocodeResponse>

    /**
     * Solicitud para obtener los detalles de un lugar específico utilizando su identificador único.
     *
     * @param placeId El identificador único del lugar cuya información se desea obtener.
     * @param apiKey La clave de API de Google necesaria para autenticar la solicitud.
     * @return Un objeto `Call` que contiene la respuesta `PlaceDetailsResponse` con los detalles del lugar.
     */
    @GET("maps/api/place/details/json")
    fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): Call<PlaceDetailsResponse>
}
