package com.example.nurture_nest.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "parent", // default role
    val createdAt: Long = System.currentTimeMillis()
)
