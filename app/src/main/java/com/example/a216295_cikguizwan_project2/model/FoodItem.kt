package com.example.a216295_cikguizwan_project2.model

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = 0L
)