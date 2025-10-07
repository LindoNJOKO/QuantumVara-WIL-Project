package com.example.nurture_nest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Request model for Stripe payment intent
data class PaymentRequest(val amount: Int)

// Response model from backend (contains Stripe client secret)
data class PaymentResponse(val clientSecret: String)

// Retrofit service for payment-related endpoints
interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("create-payment-intent")
    fun createPaymentIntent(
        @Body request: PaymentRequest
    ): Call<PaymentResponse>
}
