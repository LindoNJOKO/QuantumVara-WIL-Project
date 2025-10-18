package com.example.nurture_nest

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val eventType: String = "", // "Meeting", "Trip", "Sports Day"
    val date: Long = 0L, // timestamp
    val time: String = "",
    val location: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)