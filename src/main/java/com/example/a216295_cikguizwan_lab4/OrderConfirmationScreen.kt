package com.example.a216295_cikguizwan_lab4

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun OrderConfirmationScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    // Get final details from ViewModel
    val lastItem = viewModel.cartItems.lastOrNull()
    val foodName = lastItem?.first?.name ?: "No item"
    val totalAmount = viewModel.getCartTotal()

    // 1. We use a Box with fillMaxSize to take up the WHOLE screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)), // Light background to make the card pop
        contentAlignment = Alignment.Center // THIS CENTERS EVERYTHING
    ) {
        // 2. Wrap everything in a Card so it doesn't look like plain text
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Card takes up 85% of screen width
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Centers items inside card
            ) {
                // 3. THE GREEN TICK ICON
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF20C437), // Green Success Color
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. THE "WOW" HEADER
                Text(
                    text = "Order Confirmed!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )

                Text(
                    text = "Your tastebuds are in for a treat!\nWe are on our way to serve you something delicious. \uD83D\uDE0B",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp, // Adds spacing between lines for better "tersusun" look
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 5. SUMMARY DETAILS (Organized)
                DetailRow(label = "Ordered:", value = foodName)
                DetailRow(label = "Total Amount:", value = "RM ${String.format("%.2f", totalAmount)}")

                Spacer(modifier = Modifier.height(32.dp))

                // 6. BACK TO HOME BUTTON (Mealify Purple)
                Button(
                    onClick = {
                        viewModel.cartItems.clear()
                        navController.navigate("main_ui") {
                            popUpTo("main_ui") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAB15A3)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Back to Home",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper Composable to keep the rows clean
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}