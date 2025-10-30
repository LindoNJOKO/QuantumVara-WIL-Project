package com.example.nurture_nest.model

import com.google.firebase.Timestamp

data class Announcement(
    var title: String = "",
    var message: String = "",
    var timestamp: Timestamp? = null,
    var urgent: Boolean = false,
    var createdBy: String = ""
)