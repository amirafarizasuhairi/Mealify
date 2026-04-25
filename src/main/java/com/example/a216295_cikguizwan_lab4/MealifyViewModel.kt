package com.example.a216295_cikguizwan_lab4

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. DATA CLASSES
data class OrderData(
    val customerName: String = "",
    val deliveryAddress: String = "",
    val contactNumber: String = ""
)

// Profile Data Class
data class UserProfile(
    val firstName: String = "Amira",
    val lastName: String = "Fariza",
    val email: String = "amirafariza245@gmail.com",
    val phone: String = "+60183731400"
)

// 2. THE VIEWMODEL
class MealifyViewModel : ViewModel() {

    // --- ORDER & CART STATE ---
    private val _uiState = MutableStateFlow(OrderData())
    val uiState = _uiState.asStateFlow()

    var cartItems = mutableStateListOf<Pair<FoodItem, Int>>()

    // --- USER PROFILE STATE (For Account Section) ---
    // Inside MealifyViewModel.kt
    var userProfile by mutableStateOf(UserProfile())

    fun updateProfile(newProfile: UserProfile) {
        userProfile = newProfile // This triggers the UI to refresh!
    }

    // Update Order Details (From Form)
    fun updateOrder(name: String, address: String, phone: String) {
        _uiState.update { it.copy(
            customerName = name,
            deliveryAddress = address,
            contactNumber = phone
        )}
    }

    // Add to Cart Logic
    fun addToCart(item: FoodItem, quantity: Int) {
        val index = cartItems.indexOfFirst { it.first.name == item.name }
        if (index != -1) {
            val currentQty = cartItems[index].second
            cartItems[index] = item to (currentQty + quantity)
        } else {
            cartItems.add(item to quantity)
        }
    }

    fun getTotalItemsInCart(): Int {
        return cartItems.sumOf { it.second }
    }

    fun getCartTotal(): Double = cartItems.sumOf { (f, q) ->
        f.price.replace("RM ", "").toDoubleOrNull()?.times(q) ?: 0.0
    }
    // Inside MealifyViewModel.kt
    fun completeOrder(foodName: String) {
        // This updates the profile data that is shared across the whole app
        userProfile = userProfile.copy(firstName = foodName)
    }

}