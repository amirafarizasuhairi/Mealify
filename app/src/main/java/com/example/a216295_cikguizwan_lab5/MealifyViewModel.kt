package com.example.a216295_cikguizwan_lab5

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
// VIEWMODEL UTAMA (MENGGUNAKAN TRIK SECONDARY CONSTRUCTOR)
// ==========================================
class MealifyViewModel(private var repository: CartRepository?) : ViewModel() {

    // 🛠️ TRIK PENYELAMAT: Menyediakan constructor kosong supaya fungsi `viewModel()` pada skrin lain tidak Error!
    constructor() : this(null)

    private val _uiState = MutableStateFlow(OrderData())
    val uiState = _uiState.asStateFlow()

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

    // Pembolehubah repository tempatan yang akan di-set secara selamat
    private var localRepository: CartRepository? = null

    // 🛠️ FUNGSI INI KITA KEMASKINI SUPAYA IA MENYEDIAKAN DATABASE SECARA AUTOMATIK
    fun initRepository(context: android.content.Context) {
        if (this.localRepository == null) {
            val database = MealifyDatabase.getDatabase(context.applicationContext)
            this.localRepository = CartRepository(database.cartDao())
        }
    }

    // Membaca data secara live dari Room DB menggunakan localRepository yang selamat
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

    // Fungsi simpan item ke Room DB
    fun addItemToRoomCart(name: String, category: String, price: Double, quantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val repo = localRepository
            if (repo != null) {
                repo.addToCart(
                    CartItem(name = name, category = category, price = price, quantity = quantity)
                )
            } else {
                // SANGAT PENTING: Jika ia dipanggil sebelum initRepository, log ini akan keluar membantu kita
                android.util.Log.e(
                    "MealifyError",
                    "Repository masih null semasa mahu menyimpan data!"
                )
            }
        }
    }

    // Fungsi kosongkan Room DB selepas Place Order
    fun checkoutAndClearRoomCart() {
        viewModelScope.launch(Dispatchers.IO) {
            localRepository?.emptyCart()
        }
    }
}