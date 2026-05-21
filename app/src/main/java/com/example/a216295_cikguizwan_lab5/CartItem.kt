package com.example.a216295_cikguizwan_lab5

import androidx.room.Entity
import androidx.room.PrimaryKey

// Menetapkan nama jadual pangkalan data untuk menyimpan barangan troli
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val timestamp: Long = System.currentTimeMillis()
)