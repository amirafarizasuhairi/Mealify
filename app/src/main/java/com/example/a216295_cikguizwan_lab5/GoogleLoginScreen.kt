package com.example.a216295_cikguizwan_lab5

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleLoginScreen(navController: NavHostController, viewModel: MealifyViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Teks Tajuk Google Berwarna-warni yang Sebenar
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(text = "G", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
            Text(text = "o", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
            Text(text = "o", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBC05))
            Text(text = "g", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
            Text(text = "l", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34A853))
            Text(text = "e", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
        }

        Text(
            text = "Sign in to Mealify with your Google Account",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Input Email Google
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Google") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4285F4),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Kata Laluan Berserta Ikon Tukar Penglihatan (Show / Hide)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter your password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val buttonText = if (passwordVisible) "Hide" else "Show"

                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = Color(0xFF4285F4),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✨ SELESAI: Checkbox dibuang & Forgot Password dialihkan ke penjuru kanan skrin
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Forgot password",
                color = Color(0xFF4285F4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Butang Next
        Button(
            onClick = {
                if (email.contains("@") && password.length >= 4) {
                    errorMessage = ""
                    viewModel.loginWithGoogle(
                        emailInput = email,
                        passwordInput = password,
                        onSuccess = {
                            navController.navigate("account") {
                                popUpTo("google_login") { inclusive = true }
                            }
                        },
                        onFailure = { error ->
                            errorMessage = error
                        }
                    )
                } else {
                    errorMessage = "Sila masukkan email yang sah & kata laluan (min 4 aksara)"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Next", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✨ SELESAI: Klik Sign up sekarang akan pergi ke halaman editprofile
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "Sign up",
                color = Color(0xFF4285F4),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navController.navigate("edit_profile")
                }
            )
        }
    }
}