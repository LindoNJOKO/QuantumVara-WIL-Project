package com.example.nurture_nest.model

data class StudentAttendance(
    val name: String,
    var status: String? = null // Present, Absent, Late, Excused
)
