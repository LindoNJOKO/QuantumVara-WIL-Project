package com.example.nurture_nest.models

data class StudentAttendance(
    val name: String,
    var status: String? = null // Present, Absent, Late, Excused
)
