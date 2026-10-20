package com.heathealth.app.network

import android.os.Build
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Development Network Configuration for HeatHealth Local Backend Integration.
 * Provides multi-endpoint candidate fallback logic (USB Reverse, Wi-Fi IP, and Emulator Loopback).
 */
object BackendApiConfig {
    const val USB_REVERSE_BASE_URL = "http://127.0.0.1:8000/"
    const val EMULATOR_BASE_URL = "http://10.0.2.2:8000/"
    const val PHONE_WIFI_BASE_URL = "http://10.92.1.9:8000/"
    const val HEALTH_ENDPOINT = "health"

    fun isEmulator(): Boolean {
        return try {
            val brand = Build.BRAND ?: ""
            val device = Build.DEVICE ?: ""
            val fingerprint = Build.FINGERPRINT ?: ""
            val hardware = Build.HARDWARE ?: ""
            val model = Build.MODEL ?: ""
            val manufacturer = Build.MANUFACTURER ?: ""
            val product = Build.PRODUCT ?: ""

            (brand.startsWith("generic") && device.startsWith("generic"))
                    || fingerprint.startsWith("generic")
                    || fingerprint.startsWith("unknown")
                    || hardware.contains("goldfish")
                    || hardware.contains("ranchu")
                    || model.contains("google_sdk")
                    || model.contains("Emulator")
                    || model.contains("Android SDK built for x86")
                    || manufacturer.contains("Genymotion")
                    || product.contains("sdk_gphone")
                    || product.contains("google_sdk")
        } catch (e: Throwable) {
            false
        }
    }

    fun getCandidateBaseUrls(): List<String> {
        return if (isEmulator()) {
            listOf(EMULATOR_BASE_URL, USB_REVERSE_BASE_URL, PHONE_WIFI_BASE_URL)
        } else {
            listOf(USB_REVERSE_BASE_URL, PHONE_WIFI_BASE_URL, EMULATOR_BASE_URL)
        }
    }

    var baseUrl: String = getCandidateBaseUrls().first()

    fun createApiService(customBaseUrl: String = baseUrl): HeatHealthApiService {
        val url = if (customBaseUrl.endsWith("/")) customBaseUrl else "$customBaseUrl/"

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HeatHealthApiService::class.java)
    }
}
