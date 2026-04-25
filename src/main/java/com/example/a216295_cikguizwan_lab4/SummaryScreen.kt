package com.example.a216295_cikguizwan_lab4

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.navigation.NavHostController

@Composable
fun SummaryScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        Text("Order Summary")
        Text("Name: ${uiState.customerName}")
        Text("Address: ${uiState.deliveryAddress}")
        Text("Contact: ${uiState.contactNumber}")

        Button(onClick = { navController.popBackStack() }) {
            Text("Edit Details")
        }
    }
}