package com.example.nurture_nest.model

data class Chat(
    val id: String = "",              // UID of the other participant
    val name: String = "",            // Display name
    val email: String = "",           // Optional email
    val lastMessage: String = "No messages yet", // Last message preview
    val lastMessageTime: Long = 0L    // Timestamp of last message
)
