package com.example.nurture_nest.model

data class Meal(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val category: String = "", // Main, Vegetarian, Soup, etc.
    val dayOfWeek: String = "", // Monday, Tuesday, etc.
    val price: Double = 0.0,
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
