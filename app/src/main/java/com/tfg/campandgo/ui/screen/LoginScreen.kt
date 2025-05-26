package com.tfg.campandgo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.CustomButton

/**
 * Una función composable que representa la pantalla de inicio de sesión.
 * Esta pantalla permite a los usuarios introducir su correo electrónico y contraseña, iniciar sesión,
 * acceder con Google o navegar a la pantalla de registro.
 *
 * @param onLoginClick Callback que se activa cuando el usuario inicia sesión con éxito.
 * @param onRegisterClick Callback que se activa cuando el usuario elige registrarse.
 * @param onGoogleSignInClick Callback que se activa cuando el usuario elige acceder con Google.
 */
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    // Variables de estado para las entradas del usuario y elementos de la interfaz
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /**
         * Campo de texto para el correo electrónico.
         * Permite al usuario introducir su dirección de correo.
         */
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        /**
         * Campo de texto para la contraseña.
         * Permite al usuario introducir su contraseña. Incluye un icono para alternar visibilidad.
         */
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Mensaje de error.
         * Muestra un mensaje de error si falla el inicio de sesión.
         */
        errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        /**
         * Botón de inicio de sesión.
         * Valida las entradas e intenta iniciar sesión usando Firebase Authentication.
         */
        CustomButton(
            text = "Login",
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields."
                } else {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
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
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Botón para acceder con Google.
         * Permite al usuario iniciar sesión usando su cuenta de Google.
         */
        CustomButton(
            text = "Sign in with Google",
            onClick = onGoogleSignInClick,
            backgroundColor = Color(0xFF4285F4), // Azul de Google
            contentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Botón de registro.
         * Navega al usuario a la pantalla de registro.
         */
        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Sign up", color = MaterialTheme.colorScheme.primary)
        }
    }
}
