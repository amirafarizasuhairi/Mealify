package com.example.a216295_cikguizwan_lab4

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun EditProfileScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    // 1. Get current values from ViewModel
    val currentProfile = viewModel.userProfile

    // 2. Local state variables (Fixed the 'it' logic)
    var firstName by remember { mutableStateOf(currentProfile.firstName) }
    var lastName by remember { mutableStateOf(currentProfile.lastName) }
    var email by remember { mutableStateOf(currentProfile.email) }
    var phone by remember { mutableStateOf(currentProfile.phone) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // --- GREEN HEADER ---
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFF9C27B0)),
            contentAlignment = Alignment.Center
        ) {
            // Back Button so you don't get stuck
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            Surface(modifier = Modifier.size(90.dp), shape = CircleShape, color = Color.LightGray.copy(alpha = 0.5f)) {
                Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.padding(25.dp))
            }
        }

        // --- FORM SECTION ---
        Column(modifier = Modifier.padding(20.dp)) {
            // FIXED: Removed 'val it = ""' so typing actually works!
            ProfileInputField(label = "First name", value = firstName) { firstName = it }
            ProfileInputField(label = "Last name", value = lastName) { lastName = it }
            ProfileInputField(label = "Email", value = email) { email = it }
            ProfileInputField(label = "Phone", value = phone) { phone = it }

            Spacer(modifier = Modifier.height(40.dp))

            // --- UPDATE BUTTON ---
            Button(
                onClick = {
                    // 1. SAVE THE DATA
                    viewModel.updateProfile(
                        UserProfile(firstName, lastName, email, phone)
                    )

                    // 2. NAVIGATE BACK (Goes to Account page automatically)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)), // Purple as per your code
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Update", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

// FIXED: Removed the TODO and actually implemented the TextField
@Composable
fun ProfileInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F8E9),
                unfocusedContainerColor = Color(0xFFF1F8E9),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}