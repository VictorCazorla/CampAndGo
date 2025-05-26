package com.tfg.campandgo.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.tfg.campandgo.CustomButton

/**
 * Una función composable que representa la pantalla de inicio.
 * Proporciona opciones para que el usuario inicie sesión, acceda con Google,
 * o se registre en la aplicación CampAndGo.
 *
 * @param onLoginClick Callback que se ejecuta cuando el usuario selecciona "Login".
 * @param onGoogleSignInClick Callback que se ejecuta cuando el usuario selecciona "Login with Google".
 * @param onRegisterClick Callback que se ejecuta cuando el usuario selecciona "Register".
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StartScreen(
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título de bienvenida. Muestra un texto de introducción al usuario.
        Text(
            text = "Welcome to CampAndGo!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )

        //Botón para iniciar sesión. Permite al usuario acceder a la pantalla de inicio de sesión.
        CustomButton(
            text = "Login",
            onClick = onLoginClick,
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Botón para iniciar sesión con Google. Permite al usuario autenticarse utilizando su cuenta de Google.
        CustomButton(
            text = "Login with Google",
            onClick = onGoogleSignInClick,
            backgroundColor = Color(0xFF4285F4), // Azul de Google
            contentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Botón para registrarse. Permite al usuario navegar a la pantalla de registro.
        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register", color = MaterialTheme.colorScheme.primary)
        }
    }
}
