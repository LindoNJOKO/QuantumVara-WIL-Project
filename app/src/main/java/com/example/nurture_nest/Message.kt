package com.example.nurture_nest

data class Message(
    val text: String,
    val isSent: Boolean // true = sent by current user, false = received
)
