package com.example.a216295_cikguizwan_project2

import kotlinx.coroutines.flow.Flow

// Repository bertindak sebagai perantara ViewModel dan DAO [cite: 143, 145, 146]
class CartRepository(private val cartDao: CartDao) {
    val allItems: Flow<List<CartItem>> = cartDao.getAllCartItems()

    suspend fun addToCart(cartItem: CartItem) {
        cartDao.insertToCart(cartItem)
    }

    suspend fun emptyCart() {
        cartDao.clearCart()
    }
}