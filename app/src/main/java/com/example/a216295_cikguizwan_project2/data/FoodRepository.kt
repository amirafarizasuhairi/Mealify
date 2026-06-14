package com.example.a216295_cikguizwan_project2.data

import com.example.a216295_cikguizwan_project2.model.FoodItem
import com.google.firebase.firestore.FirebaseFirestore

class FoodRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val foodCollection = firestore.collection("donated_meals")

    fun uploadDonatedFood(
        food: FoodItem,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val newDocumentRef = foodCollection.document()
        val foodWithId = food.copy(id = newDocumentRef.id)

        newDocumentRef.set(foodWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}