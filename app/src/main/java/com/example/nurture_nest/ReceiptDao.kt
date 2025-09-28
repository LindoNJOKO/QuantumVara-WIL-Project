package com.example.nurture_nest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReceiptDao {
    @Insert
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAllReceipts(): List<ReceiptEntity>
}