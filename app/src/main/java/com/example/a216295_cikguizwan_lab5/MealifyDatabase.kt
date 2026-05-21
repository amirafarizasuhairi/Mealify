package com.example.a216295_cikguizwan_lab5

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartItem::class], version = 1, exportSchema = false)
abstract class MealifyDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao // Menyertakan DAO ke dalam DB [cite: 142]

    companion object {
        @Volatile
        private var Instance: MealifyDatabase? = null

        // Membina Singleton Instance untuk mengelakkan pembaziran memori [cite: 141]
        fun getDatabase(context: Context): MealifyDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MealifyDatabase::class.java,
                    "mealify_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}