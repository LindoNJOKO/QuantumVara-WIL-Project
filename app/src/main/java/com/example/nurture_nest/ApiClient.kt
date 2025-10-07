package com.example.nurture_nest

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ApiClient handles Retrofit setup for Stripe payment backend.
 *
 * ⚠️ NOTE:
 * - When using the **Android Emulator**, keep `10.0.2.2` (maps to localhost of your PC).
 * - When testing on a **physical device**, replace this IP with your PC’s local IP.
 *   Example: http://10.0.0.114:5000/
 */
object ApiClient {

    // Local backend endpoint for Stripe (for physical phone testing)
    private const val BASE_URL = "http://10.0.0.114:5000/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
