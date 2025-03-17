package com.tfg.campandgo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Hacemos uso del patrón Singleton
 */
object RetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/"
    private var retrofit: Retrofit? = null

    private fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    val placesService: GooglePlacesService by lazy {
        getRetrofitInstance().create(GooglePlacesService::class.java)
    }

    val geocodeService: GeocodeService by lazy {
        getRetrofitInstance().create(GeocodeService::class.java)
    }
}