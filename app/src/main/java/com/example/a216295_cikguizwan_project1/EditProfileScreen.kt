package com.example.a216295_cikguizwan_project1

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.a216295_cikguizwan_project1.R // Replace with your actual package name if different
@Composable
fun EditProfileScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    // 1. Get current values from ViewModel
    val currentProfile = viewModel.userProfile

    // 2. Local state variables (Fixed the 'it' logic)
    var firstName by remember { mutableStateOf(currentProfile.firstName) }
    var lastName by remember { mutableStateOf(currentProfile.lastName) }
    var email by remember { mutableStateOf(currentProfile.email) }
    var phone by remember { mutableStateOf(currentProfile.phone) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {

        // --- 1. HEADER (Matches AccountScreen) ---
        // --- UPDATED HEADER FOR EDIT PROFILE SCREEN ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB02CAC))
                .statusBarsPadding() // Ensures content is below the camera/clock
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp, top = 40.dp, bottom = 40.dp) // Large top/bottom padding for "Gap"
            ) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Update your information",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- 2. THE FORM SECTION ---
        // Wrap this in a Column with padding so it stays BELOW the header
        Column(modifier = Modifier.padding(20.dp)) {
            ProfileInputField("First name", firstName) { firstName = it }
            ProfileInputField("Last name", lastName) { lastName = it }
            ProfileInputField("Email", email) { email = it }
            ProfileInputField("Phone", phone) { phone = it }

            Spacer(modifier = Modifier.height(30.dp))

            // Update Button
            Button(
                onClick = {
                    viewModel.updateProfile(UserProfile(firstName, lastName, email, phone))
                    navController.navigate("account") {
                        popUpTo("account") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB02CAC)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Update", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BACK TO HOME BUTTON ---
            OutlinedButton(
                onClick = {
                    // Make sure "home" matches your NavHost route name!
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB02CAC)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Back to Home",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFB02CAC)
                )
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