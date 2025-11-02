package com.example.nurture_nest.models

data class StudentAttendance(
    val name: String = "",
    var status: String? = null,
    val teacherId: String? = null,
    val date: String? = null
)
