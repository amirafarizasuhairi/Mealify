package com.example.a216295_cikguizwan_project1

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- ONLY DEFINE THESE ONCE HERE ---
data class FoodItem(
    val image: Int, val name: String, val price: String, val originalPrice: String,
    val description: String, val category: String, val stock: String,
    val storeName: String, val time: String, val distance: String, val discount: String
)

data class OrderData(
    val customerName: String = "",
    val deliveryAddress: String = "",
    val contactNumber: String = ""
)

data class UserProfile(
    val firstName: String = "Amira",
    val lastName: String = "Fariza",
    val email: String = "amirafariza245@gmail.com",
    val phone: String = "+60183731400"
)

class MealifyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrderData())
    val uiState = _uiState.asStateFlow()
    var cartItems = mutableStateListOf<Pair<FoodItem, Int>>()
    var userProfile by mutableStateOf(UserProfile())
        private set
    fun updateProfile(newProfile: UserProfile) {
        userProfile = newProfile
    }
    fun getCartTotal(): Double = cartItems.sumOf { (f, q) ->
        f.price.replace("RM ", "").toDoubleOrNull()?.times(q) ?: 0.0
    }
    fun updateOrder(name: String, address: String, phone: String) {
        _uiState.update { it.copy(customerName = name, deliveryAddress = address, contactNumber = phone) }
    }

    fun addToCart(item: FoodItem, quantity: Int) {
        val index = cartItems.indexOfFirst { it.first.name == item.name }
        if (index != -1) {
            val currentQty = cartItems[index].second
            cartItems[index] = item to (currentQty + quantity)
        } else {
            cartItems.add(item to quantity)
        }
    }

    fun getTotalItemsInCart(): Int = cartItems.sumOf { it.second }


}