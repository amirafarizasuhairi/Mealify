package com.example.a216295_cikguizwan_project2

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// IMPORT UNTUK RETROFIT & API (LAB 6)
// ==========================================
import com.example.a216295_cikguizwan_project2.api.RetrofitInstance
import com.example.a216295_cikguizwan_project2.model.RecipeResult

// ==========================================
// DATA MODELS (DIKEKALKAN 100% TIADA TERCICIR)
// ==========================================
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

// ==========================================
// VIEWMODEL UTAMA
// ==========================================
class MealifyViewModel(private var repository: CartRepository?) : ViewModel() {

    // 🛠️ TRIK PENYELAMAT: Menyediakan constructor kosong supaya fungsi `viewModel()` pada skrin lain tidak Error!
    constructor() : this(null)

    private val _uiState = MutableStateFlow(OrderData())
    val uiState = _uiState.asStateFlow()

    // =========================================================================
    // 📍 🚀 MEMORI ALAMAT BERPUSAT (DITAMBAH UNTUK MEMBAIKI MASALAH AMIRA)
    // =========================================================================
    // Ini akan memegang nilai alamat semasa secara global supaya HomeScreen terus bertukar secara LIVE!
    // 🎯 UPDATE THIS INSIDE YOUR MEALIFYVIEWMODEL.KT
    var currentHomeAddress by mutableStateOf("") // Set to empty so the placeholder shows up!
        private set



    // ==========================================
    // 🌐 STATE & OPERASI WEB API (SPOONACULAR) - DIKEMASKINI & DITAMBAH
    // ==========================================
    private val _recipes = MutableStateFlow<List<RecipeResult>>(emptyList())
    val recipes: StateFlow<List<RecipeResult>> = _recipes.asStateFlow()

    private val _isLoadingRecipes = MutableStateFlow(false)
    val isLoadingRecipes: StateFlow<Boolean> = _isLoadingRecipes.asStateFlow()

    private val _recipeError = MutableStateFlow<String?>(null)
    val recipeError: StateFlow<String?> = _recipeError.asStateFlow()

    fun fetchRecipes(query: String = "healthy food SDG") {
        viewModelScope.launch {
            _isLoadingRecipes.value = true
            _recipeError.value = null
            try {
                val response = com.example.a216295_cikguizwan_project2.api.RetrofitInstance.api.searchRecipes(query)
                _recipes.value = response.results
            } catch (e: Exception) {
                _recipeError.value = "Failed to load recipes: ${e.message}"
                android.util.Log.e("MealifyAPI", "Error fetching from Spoonacular", e)
            } finally {
                _isLoadingRecipes.value = false
            }
        }
    }

    fun updateHomeAddress(newAddress: String) {
        currentHomeAddress = newAddress
    }


    // ADD THESE 3 LINES IF NOT ALREADY THERE:
    var pendingDonateLatitude by mutableStateOf(2.9289)
    var pendingDonateLongitude by mutableStateOf(101.7812)
    var pendingDonateAddress by mutableStateOf("Bangi, Selangor")


    // Memori Cart Sementara (Dikekalkan untuk skrin lama anda)
    var cartItems = mutableStateListOf<Pair<FoodItem, Int>>()

    // Status Berpusat untuk Profil Pengguna (Dikekalkan)
    var userProfile by mutableStateOf(UserProfile())
        private set

    fun updateProfile(newProfile: UserProfile) {
        userProfile = newProfile
    }

    fun getCartTotal(): Double = cartItems.sumOf { (f, q) ->
        f.price.replace("RM ", "").toDoubleOrNull()?.times(q) ?: 0.0
    }

    fun updateOrder(name: String, address: String, phone: String) {
        _uiState.update {
            it.copy(
                customerName = name,
                deliveryAddress = address,
                contactNumber = phone
            )
        }
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

    // Fungsi Login Google
    fun loginWithGoogle(
        emailInput: String,
        passwordInput: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (emailInput.contains("@") && passwordInput.length >= 4) {
            userProfile = userProfile.copy(email = emailInput)
            onSuccess()
        } else {
            onFailure("E-mel atau kata laluan tidak sah.")
        }
    }

    fun logout() {
        userProfile = UserProfile("", "", "", "")
    }

    // ==========================================
    // 🛒 INTEGRASI ROOM DATABASE (LAB 5 KEPERLUAN UTAMA)
    // ==========================================

    private var localRepository: CartRepository? = null

    fun initRepository(context: android.content.Context) {
        if (this.localRepository == null) {
            val database = MealifyDatabase.getDatabase(context.applicationContext)
            this.localRepository = CartRepository(database.cartDao())
        }
    }

    val cartItemsState: StateFlow<List<CartItem>> by lazy {
        val repo = localRepository
        if (repo != null) {
            repo.allItems.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        } else {
            MutableStateFlow(emptyList())
        }
    }

    fun addItemToRoomCart(name: String, category: String, price: Double, quantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val repo = localRepository
            if (repo != null) {
                repo.addToCart(
                    CartItem(name = name, category = category, price = price, quantity = quantity)
                )
            } else {
                android.util.Log.e(
                    "MealifyError",
                    "Repository masih null semasa mahu menyimpan data!"
                )
            }
        }
    }

    fun checkoutAndClearRoomCart() {
        viewModelScope.launch(Dispatchers.IO) {
            localRepository?.emptyCart()
        }
    }
}