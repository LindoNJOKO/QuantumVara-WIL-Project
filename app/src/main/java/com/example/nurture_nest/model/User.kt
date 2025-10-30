package com.example.nurture_nest.model

import com.google.firebase.Timestamp

data class User(
    val cellphone: String = "",
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Parent",
    val createdAt: Timestamp = Timestamp.now()
)
