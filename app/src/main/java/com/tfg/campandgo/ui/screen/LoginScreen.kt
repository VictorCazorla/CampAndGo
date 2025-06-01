package com.tfg.campandgo.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.CustomButton
import com.tfg.campandgo.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    // State variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val auth = Firebase.auth

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.login_fondo),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark overlay for better text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.5f)
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome text
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.White.copy(alpha = 0.8f)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.White), // ¡Aquí se establece el color del texto!
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedBorderColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White.copy(alpha = 0.8f)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = Color.White
                        )
                    }
                },
                textStyle = LocalTextStyle.current.copy(color = Color.White), // ¡Aquí se establece el color del texto!
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedBorderColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF5252), // Material Red 400
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Login button
            CustomButton(
                text = "Login",
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields."
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoginClick()
                                } else {
                                    errorMessage = when (task.exception) {
                                        is FirebaseAuthInvalidUserException -> {
                                            "No account was found with this email."
                                        }
                                        is FirebaseAuthInvalidCredentialsException -> {
                                            when ((task.exception as FirebaseAuthInvalidCredentialsException).errorCode) {
                                                "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                                                "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                                                else -> "Invalid credentials. Please check your email and password."
                                            }
                                        }
                                        else -> {
                                            "Login failed. Please verify your email and password."
                                        }
                                    }
                                }
                            }
                    }
                },
                backgroundColor = Color.Gray.copy(alpha = 0.7f),
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "OR",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In button
            CustomButton(
                text = "Continue with Google",
                onClick = onGoogleSignInClick,
                backgroundColor = Color.White,
                contentColor = Color(0xFF757575), // Google gray
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "Google icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Don't have an account?",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "Sign up",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}