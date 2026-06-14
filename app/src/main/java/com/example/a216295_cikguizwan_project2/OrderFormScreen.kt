package com.example.a216295_cikguizwan_project2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun OrderFormScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    val context = LocalContext.current
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
                        // 1. Basic validation — don't submit empty delivery details
                        if (uiState.customerName.isBlank() || uiState.contactNumber.isBlank() || uiState.deliveryAddress.isBlank()) {
                            android.widget.Toast.makeText(context, "Please fill in all delivery details", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                        // Auto-generate a unique order ID
                        val orderRef = db.collection("users")
                            .document(userId)
                            .collection("orders")
                            .document()

                        // Build the items list from the cart (cartItems is a List<Pair<FoodItem, Int>>)
                        val itemsList = viewModel.cartItems.map { (food, qty) ->
                            hashMapOf(
                                "name" to food.name,
                                "category" to food.category,
                                "price" to (food.price.replace("RM ", "").toDoubleOrNull() ?: 0.0),
                                "quantity" to qty
                            )
                        }

                        val orderData = hashMapOf(
                            "orderId" to orderRef.id,
                            "customerName" to uiState.customerName,
                            "contactNumber" to uiState.contactNumber,
                            "deliveryAddress" to uiState.deliveryAddress,
                            "items" to itemsList,
                            "totalAmount" to totalAmount,
                            "status" to "Pending",
                            "timestamp" to System.currentTimeMillis()
                        )

                        // Save to Firestore
                        orderRef.set(orderData)
                            .addOnSuccessListener {
                                navController.navigate("confirmation")
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(context, "Failed to save order: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Place Order", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
