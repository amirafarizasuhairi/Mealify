package com.example.a216295_cikguizwan_project2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    // Memasukkan item ke dalam troli [cite: 137]
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToCart(cartItem: CartItem)

    // Mengambil semua data item troli secara langsung (Real-time Flow) [cite: 138]
    @Query("SELECT * FROM cart_items ORDER BY timestamp DESC")
    fun getAllCartItems(): Flow<List<CartItem>>

    // Mengosongkan semua item di dalam troli [cite: 139]
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}