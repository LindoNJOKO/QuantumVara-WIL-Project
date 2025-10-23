package com.example.nurture_nest

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val item: String,
    val amount: Double,
    val date: Long,
    val userId: String = ""
)

