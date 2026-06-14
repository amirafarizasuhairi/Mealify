package com.example.a216295_cikguizwan_project2.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a216295_cikguizwan_project2.data.FoodRepository
import com.example.a216295_cikguizwan_project2.model.FoodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateFoodScreen(
    initialLatitude: Double,
    initialLongitude: Double,
    initialAddress: String,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FoodRepository() }

    // State managers pre-populated with map data passed from MainActivity
    var foodName by remember { mutableStateOf("") }
    var foodPrice by remember { mutableStateOf("") }
    val latitude by remember { mutableStateOf(initialLatitude) }
    val longitude by remember { mutableStateOf(initialLongitude) }
    var addressText by remember { mutableStateOf(initialAddress) }
    var isUploading by remember { mutableStateOf(false) }

    val mealifyDarkPurple = Color(0xFFB02CAC)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donate Surplus Food", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = mealifyDarkPurple)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 📝 Food Details Entry Group
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Item Name") },
                placeholder = { Text("e.g., Delicious Strawberry Matcha Slice") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = mealifyDarkPurple)
            )

            OutlinedTextField(
                value = foodPrice,
                onValueChange = { foodPrice = it },
                label = { Text("Estimated Retail Value (RM)") },
                placeholder = { Text("e.g., 15.00") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = mealifyDarkPurple)
            )

            // 🗺️ Visual Map Data Information Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Map Pin", tint = mealifyDarkPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Confirmed Map Location:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = addressText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray)

                    Text(text = "Latitude: $latitude", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "Longitude: $longitude", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 🚀 Final Cloud Firebase Dispatch Button
            Button(
                onClick = {
                    if (foodName.trim().isEmpty() || foodPrice.trim().isEmpty()) {
                        Toast.makeText(context, "Please fill in all details before submitting!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isUploading = true

                    val newFood = FoodItem(
                        name = foodName,
                        price = "RM ${foodPrice.replace("RM", "").trim()}",
                        latitude = latitude,
                        longitude = longitude,
                        timestamp = System.currentTimeMillis()
                    )

                    repository.uploadDonatedFood(
                        food = newFood,
                        onSuccess = {
                            isUploading = false
                            Toast.makeText(context, "Donation uploaded to Mealify Cloud Firestore!", Toast.LENGTH_LONG).show()
                            // Clear form values and head safely back to the home view
                            foodName = ""
                            foodPrice = ""
                            onNavigateBack()
                        },
                        onFailure = { error ->
                            isUploading = false
                            Toast.makeText(context, "Firebase Upload Error: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mealifyDarkPurple),
                shape = RoundedCornerShape(24.dp),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Surplus Food Donation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}