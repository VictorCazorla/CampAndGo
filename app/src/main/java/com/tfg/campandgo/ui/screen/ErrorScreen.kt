package com.tfg.campandgo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Este componente es utilizado para mostrar mensajes de error en la interfaz de usuario.
 * El mensaje se muestra en el centro de la pantalla con un color de error definido por el tema.
 *
 * @param message El mensaje de error que se desea mostrar. No puede ser nulo.
 */
@Composable
fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}