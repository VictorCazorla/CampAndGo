package com.tfg.campandgo.ui.screen

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
    var email by remember { mutableStateOf("") } // Entrada del correo electrónico
    var password by remember { mutableStateOf("") } // Entrada de la contraseña
    var errorMessage by remember { mutableStateOf<String?>(null) } // Mensaje de error a mostrar
    var isPasswordVisible by remember { mutableStateOf(false) } // Alterna la visibilidad de la contraseña
    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Padding de la pantalla
        verticalArrangement = Arrangement.Center, // Centra el contenido verticalmente
        horizontalAlignment = Alignment.CenterHorizontally // Centra el contenido horizontalmente
    ) {
        /**
         * Campo de texto para el correo electrónico.
         * Permite al usuario introducir su dirección de correo.
         */
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp)) // Espacio entre campos

        /**
         * Campo de texto para la contraseña.
         * Permite al usuario introducir su contraseña. Incluye un icono para alternar visibilidad.
         */
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
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
            text = "Iniciar sesión",
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor, rellena todos los campos."
                } else {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //TODO pasar info a preference
                            onLoginClick()
                        } else {
                            when (task.exception) {
                                is FirebaseAuthInvalidUserException -> {
                                    errorMessage = "No se encontró una cuenta con este correo."
                                }
                                is FirebaseAuthInvalidCredentialsException -> {
                                    errorMessage = when ((task.exception as FirebaseAuthInvalidCredentialsException).errorCode) {
                                        "ERROR_INVALID_EMAIL" -> "La dirección de correo electrónico está mal formada."
                                        "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta. Inténtalo de nuevo."
                                        else -> "Credenciales inválidas. Por favor, verifica tu correo y contraseña."
                                    }
                                }
                                else -> {
                                    errorMessage = "El inicio de sesión falló. Por favor, verifica tu correo y contraseña."
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
            text = "Acceder con Google",
            onClick = onGoogleSignInClick,
            backgroundColor = Color(0xFF4285F4), // Azul de Google
            contentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Ícono de Google",
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
            Text("¿No tienes una cuenta? Regístrate", color = MaterialTheme.colorScheme.primary)
        }
    }
}
