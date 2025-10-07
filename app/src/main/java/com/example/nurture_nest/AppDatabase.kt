package com.example.nurture_nest

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for Nurture Nest.
 * Stores receipts for Stripe / Firestore payments locally.
 */
@Database(entities = [ReceiptEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun receiptDao(): ReceiptDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Returns the singleton AppDatabase instance.
         * Ensures thread safety and single DB instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nurture_nest_db"
                )
                    // Optional for development to reset DB on schema changes:
                    // .fallbackToDestructiveMigration()
                    .build()

                instance = db
                db
            }
        }
    }
}
