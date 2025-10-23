package com.example.nurture_nest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @POST("/create-payment-intent")
    fun createPaymentIntent(@Body request: PaymentRequest): Call<PaymentResponse>
}

data class PaymentRequest(
    val amount: Int
)

data class PaymentResponse(
    val clientSecret: String
)