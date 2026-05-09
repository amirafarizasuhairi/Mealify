package com.example.a216295_cikguizwan_project1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun OrderFormScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Data from ViewModel
    val lastItem = viewModel.cartItems.lastOrNull()
    val foodName = lastItem?.first?.name ?: "No item"
    val totalAmount = viewModel.getCartTotal()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7)) // Subtle grey background makes the card "pop"
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. HEADER (Top of screen) ---
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Order: $foodName",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Total: RM ${String.format("%.2f", totalAmount)}",
            color = Color(0xFF3F51B5),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Increase this number to push the form even lower
        Spacer(modifier = Modifier.height(80.dp))

        // --- 3. THE BEAUTIFUL CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delivery Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Receiver Name
                OutlinedTextField(
                    value = uiState.customerName,
                    onValueChange = { viewModel.updateOrder(it, uiState.deliveryAddress, uiState.contactNumber) },
                    label = { Text("Receiver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Number
                OutlinedTextField(
                    value = uiState.contactNumber,
                    onValueChange = { viewModel.updateOrder(uiState.customerName, uiState.deliveryAddress, it) },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Address
                OutlinedTextField(
                    value = uiState.deliveryAddress,
                    onValueChange = { viewModel.updateOrder(uiState.customerName, it, uiState.contactNumber) },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- 4. THE PURPLE BUTTON IN ORDERFORMSCREEN ---
                Button(
                    onClick = {
                        // Just navigate to confirmation. DO NOT update the profile here.
                        navController.navigate("confirmation")
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Place Order", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
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
                            tint = Color(0xFF9725A8), // Green Success Color
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
                            text = "Yums! Order Received! Thankyouuu 😋",
                            color = Color.Gray,
                            fontSize = 16.sp
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
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
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
    }
}