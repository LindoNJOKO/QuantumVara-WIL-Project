package com.example.nurture_nest.model

data class LunchOrder(
    val id: String = "",
    val userId: String = "",
    val childName: String = "",
    val weekStartDate: String = "", // Format: "2025-01-20"
    val meals: Map<String, MealSelection> = emptyMap(), // Day -> MealSelection
    val totalPrice: Double = 0.0,
    val status: String = "pending", // pending, confirmed, cancelled
    val orderDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)
