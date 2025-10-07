package com.example.nurture_nest

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a payment receipt stored in local Room database.
 * Used by PaymentWindow and displayed in ReceiptsActivity.
 */
@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val item: String,      // e.g., "Tuition Fee", "Lunch Order #123"
    val amount: Double,    // Payment amount in ZAR
    val date: Long         // Timestamp (System.currentTimeMillis())
)
