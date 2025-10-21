package com.example.nurture_nest.model

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
) {
    val isSent: Boolean
        get() = senderId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
}
