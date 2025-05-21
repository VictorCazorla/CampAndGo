package com.tfg.campandgo.data.api.service

import com.tfg.campandgo.data.model.AutocompleteResponse
import com.tfg.campandgo.data.model.GeocodeResponse
import com.tfg.campandgo.data.model.NearbySearchResponse
import com.tfg.campandgo.data.model.PlaceDetailsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz que define los servicios de la API de Google Places.
 * Contiene las solicitudes para autocompletado, geocodificación, detalles de lugares
 * y búsqueda de lugares cercanos utilizando Retrofit.
 */
interface GooglePlacesService {

    /**
     * Solicitud para obtener predicciones basadas en una entrada de texto.
     *
     * @param input El texto ingresado por el usuario para obtener sugerencias.
     * @param key La clave de API de Google.
     * @param language El idioma en el que se devolverán las respuestas (por defecto es "es").
     * @param components Componentes opcionales que restringen los resultados (por ejemplo, país).
     * @return Una respuesta `AutocompleteResponse` con las predicciones sugeridas.
     */
    @GET("maps/api/place/autocomplete/json")
    suspend fun autocomplete(
        @Query("input") input: String,
        @Query("key") key: String,
        @Query("language") language: String = "es",
        @Query("components") components: String = "country:es"
    ): AutocompleteResponse

    /**
     * Solicitud para geocodificar una dirección y obtener su información geográfica.
     *
     * @param address La dirección a geocodificar.
     * @param key La clave de API de Google.
     * @return Una respuesta `GeocodeResponse` con los resultados de la geocodificación.
     */
    @GET("maps/api/geocode/json")
    suspend fun geocode(
        @Query("address") address: String,
        @Query("key") key: String
    ): GeocodeResponse

    /**
     * Solicitud para obtener los detalles de un lugar específico.
     *
     * @param placeId El identificador único del lugar.
     * @param apiKey La clave de API de Google.
     * @return Una respuesta `PlaceDetailsResponse` con la información detallada del lugar.
     */
    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): PlaceDetailsResponse

    /**
     * Solicitud para buscar lugares cercanos a una ubicación específica.
     *
     * @param location La ubicación en formato "lat,lng".
     * @param radius El radio de búsqueda en metros.
     * @param type El tipo de lugar a buscar (por ejemplo, restaurante, parque, etc.).
     * @param key La clave de API de Google.
     * @param language El idioma en el que se devolverán las respuestas (por defecto es "es").
     * @return Una respuesta `NearbySearchResponse` con los lugares encontrados.
     */
    @GET("maps/api/place/nearbysearch/json")
    suspend fun nearbySearch(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String,
        @Query("language") language: String = "es"
    ): NearbySearchResponse
}
