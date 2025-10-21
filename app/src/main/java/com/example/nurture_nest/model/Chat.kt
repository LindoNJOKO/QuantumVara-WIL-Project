package com.example.nurture_nest.model

data class Chat(
    val id: String = "",              // Other user's ID
    val name: String = "",            // Other user's display name
    val email: String = "",           // Other user's email
    val lastMessage: String = "",     // Last message in the chat
    val time: Long = 0L               // Timestamp of last message
)
