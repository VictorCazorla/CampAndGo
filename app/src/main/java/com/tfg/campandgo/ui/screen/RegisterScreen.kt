package com.tfg.campandgo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.CustomButton


/**
 * Una función composable que representa la pantalla de registro.
 * Permite a los usuarios registrar una nueva cuenta ingresando su correo electrónico,
 * contraseña y confirmación de contraseña.
 *
 * @param onRegisterClick Callback que se ejecuta cuando el registro es exitoso.
 * @param onBackToLoginClick Callback que se ejecuta cuando el usuario decide regresar a la pantalla de inicio de sesión.
 */
@Composable
fun RegisterScreen(onRegisterClick: () -> Unit, onBackToLoginClick: () -> Unit) {
    // Variables de estado para la entrada de datos
    var email by remember { mutableStateOf("") } // Almacena el correo electrónico ingresado por el usuario
    var password by remember { mutableStateOf("") } // Almacena la contraseña ingresada
    var passwordVerify by remember { mutableStateOf("") } // Almacena la contraseña para la verificación
    var errorMessage by remember { mutableStateOf<String?>(null) } // Mensaje de error a mostrar en caso de validaciones fallidas
    var isPasswordVisible by remember { mutableStateOf(false) } // Alterna la visibilidad de la contraseña principal
    var isPasswordVerifyVisible by remember { mutableStateOf(false) } // Alterna la visibilidad de la confirmación de contraseña
    val auth = Firebase.auth // Instancia de autenticación de Firebase

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /**
         * Campo de texto para el correo electrónico.
         * Permite al usuario ingresar su dirección de correo.
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
         * Permite al usuario ingresar la contraseña. Incluye un icono para alternar la visibilidad.
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
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        /**
         * Campo de texto para la verificación de contraseña.
         * Permite al usuario confirmar su contraseña. Incluye un icono para alternar la visibilidad.
         */
        TextField(
            value = passwordVerify,
            onValueChange = { passwordVerify = it },
            label = { Text("Repeat password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVerifyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVerifyVisible = !isPasswordVerifyVisible }) {
                    Icon(
                        imageVector = if (isPasswordVerifyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVerifyVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Mensaje de error.
         * Muestra mensajes si ocurre un error en la validación o durante el registro.
         */
        errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        /**
         * Botón de registro.
         * Valida las entradas y registra al usuario con Firebase Authentication.
         */
        CustomButton(
            text = "Register",
            onClick = {
                if (email.isBlank() || password.isBlank() || passwordVerify.isBlank()) {
                    errorMessage = "Please fill in all fields"
                } else if (password != passwordVerify) {
                    errorMessage = "Passwords do not match"
                } else if (password.length > 10) {
                    errorMessage = "Password must be under 10 characters"
                } else if (!password.contains(Regex("\\d+"))) {
                    errorMessage = "Password must contain at least one digit."
                } else {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onRegisterClick()
                        } else {
                            when (task.exception) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    errorMessage = when ((task.exception as FirebaseAuthInvalidCredentialsException).errorCode) {
                                        "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                                        "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                                        else -> "Invalid credentials. Please check your email and password."
                                    }
                                }
                                else -> {
                                    errorMessage = "Registration failed. Please check the fields and try again."
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
         * Botón para volver al inicio de sesión.
         * Permite al usuario regresar a la pantalla de login.
         */
        TextButton(onClick = onBackToLoginClick) {
            Text("Already have an account? Login", color = MaterialTheme.colorScheme.primary)
        }
    }
}
