package com.example.nurture_nest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * DAO for accessing receipts in Room database.
 */
@Dao
interface ReceiptDao {

    /**
     * Insert a new receipt into the database.
     */
    @Insert
    suspend fun insertReceipt(receipt: ReceiptEntity)

    /**
     * Fetch all receipts ordered by date descending (most recent first).
     */
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAllReceipts(): List<ReceiptEntity>

    /**
     * Get receipts for specific users
     */
    @Query("SELECT * FROM receipts WHERE userId = :userId ORDER BY date DESC")
    suspend fun getReceiptsByUser(userId: String): List<ReceiptEntity>
}
