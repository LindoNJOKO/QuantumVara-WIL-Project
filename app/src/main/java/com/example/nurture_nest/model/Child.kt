package com.example.nurture_nest.model

data class Child(
    var id: String = "",
    var name: String = "",
    var parentId: String = "",
    var teacherId: String = "",
    var preferences: Map<String, Any> = mapOf(
        "preferredName" to "",
        "allergies" to "",
        "medicalConditions" to "",
        "visionNeeds" to "",
        "emergencyContactName" to "",
        "emergencyContactNumber" to "",
        "additionalNotes" to ""
    )
)
