package com.heathealth.app.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Development Network Configuration for HeatHealth Local Backend Integration.
 *
 * Base URLs:
 * - Physical Phone Host URL: "http://10.92.1.9:8000/"
 * - Android Emulator Host URL: "http://10.0.2.2:8000/"
 */
object BackendApiConfig {
    const val EMULATOR_BASE_URL = "http://10.0.2.2:8000/"
    const val PHONE_BASE_URL = "http://10.92.1.9:8000/"
    const val HEALTH_ENDPOINT = "health"

    var baseUrl: String = PHONE_BASE_URL

    fun createApiService(customBaseUrl: String = baseUrl): HeatHealthApiService {
        val url = if (customBaseUrl.endsWith("/")) customBaseUrl else "$customBaseUrl/"
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HeatHealthApiService::class.java)
    }
}
