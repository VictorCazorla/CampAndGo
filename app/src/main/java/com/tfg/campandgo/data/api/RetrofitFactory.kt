package com.tfg.campandgo.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton que proporciona las instancias de servicios Retrofit para la comunicación
 * con las APIs. Este objeto asegura que solo exista una única instancia de Retrofit
 * durante el ciclo de vida de la aplicación.
 */
object RetrofitFactory {

    fun create(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
