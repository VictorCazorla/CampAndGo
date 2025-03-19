package com.tfg.campandgo.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton que proporciona las instancias de servicios Retrofit para la comunicación
 * con la API de Google Maps. Este objeto asegura que solo exista una única instancia de Retrofit
 * durante el ciclo de vida de la aplicación.
 */
object RetrofitClient {

    private const val BASE_URL = "https://maps.googleapis.com/"
    private var retrofit: Retrofit? = null

    /**
     * Obtiene una instancia única de Retrofit. Si no existe, la crea con la URL base y el
     * convertidor de Gson para manejar respuestas en formato JSON.
     *
     * @return Instancia de Retrofit configurada.
     */
    private fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    /**
     * Servicio de la API de Google Places.
     * Se inicializa de forma perezosa usando la instancia única de Retrofit.
     */
    val placesService: GooglePlacesService by lazy {
        getRetrofitInstance().create(GooglePlacesService::class.java)
    }

    /**
     * Servicio de la API de Geocodificación.
     * Se inicializa de forma perezosa usando la instancia única de Retrofit.
     */
    val geocodeService: GeocodeService by lazy {
        getRetrofitInstance().create(GeocodeService::class.java)
    }
}
