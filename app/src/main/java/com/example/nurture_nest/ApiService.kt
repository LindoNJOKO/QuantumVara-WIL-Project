package com.example.nurture_nest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class PaymentRequest(val amount: Int)
data class PaymentResponse(val clientSecret: String)

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("create-payment-intent")
}