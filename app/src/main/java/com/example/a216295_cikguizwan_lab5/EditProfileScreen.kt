package com.example.a216295_cikguizwan_lab5

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController, viewModel: MealifyViewModel) {

    // Membaca nilai asal daripada objek state berpusat ViewModel
    var localFirstName by remember { mutableStateOf(viewModel.userProfile.firstName) }
    var localLastName by remember { mutableStateOf(viewModel.userProfile.lastName) }
    var localEmail by remember { mutableStateOf(viewModel.userProfile.email) }
    var localPhone by remember { mutableStateOf(viewModel.userProfile.phone) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mealify Profile", fontWeight = FontWeight.Bold, color = Color(0xFF7A1FA2)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF7A1FA2))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7A1FA2))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(70.dp)) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👩", fontSize = 36.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(text = "HI! WELCOME BACK", fontSize = 12.sp, color = Color(0xFFE1BEE7), fontWeight = FontWeight.Bold)
                        Text(text = "PROFILE INFO", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "Connected as:\n${viewModel.userProfile.email}", fontSize = 12.sp, color = Color(0xFFE1BEE7))
                    }
                }
            }

            OutlinedTextField(
                value = localFirstName,
                onValueChange = { localFirstName = it },
                label = { Text("First name :") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = localLastName,
                onValueChange = { localLastName = it },
                label = { Text("Last name :") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = localEmail,
                onValueChange = { localEmail = it },
                label = { Text("Connected Email :") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = localPhone,
                onValueChange = { localPhone = it },
                label = { Text("Phone Number :") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    // Simpan data kembali ke dalam model berpusat
                    viewModel.updateProfile(
                        UserProfile(
                            firstName = localFirstName,
                            lastName = localLastName,
                            email = localEmail,
                            phone = localPhone
                        )
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    navController.navigate("google_login") {
                        popUpTo("main_ui") { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A148C)),
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(1.dp, Color(0xFF4A148C))
            ) {
                Text(text = "Disconnect Google Account", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}