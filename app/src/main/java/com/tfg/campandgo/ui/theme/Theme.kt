package com.tfg.campandgo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.MapStyleOptions
import com.tfg.campandgo.R

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1E88E5), // Brighter blue
    secondary = Color(0xFFBB86FC), // Purple accent
    tertiary = Color(0xFF03DAC6), // Teal accent
    background = Color(0xFF121212), // Dark background
    surface = Color(0xFF1F1B24), // Surface slightly lighter than background
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)


private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0288D1), // Soft blue
    secondary = Color(0xFF03A9F4), // Light blue accent
    tertiary = Color(0xFFFFC107), // Gold accent
    background = Color(0xFFFFFFFF), // Pure white background
    surface = Color(0xFFF5F5F5), // Light gray surface
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun CampAndGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Añade esto para obtener el estilo del mapa según el tema
    val mapStyle = rememberMapStyle(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        // Proporciona el estilo del mapa a través de la composición local
        CompositionLocalProvider(
            LocalMapStyle provides mapStyle,
            content = content
        )
    }
}

// Función para cargar el estilo adecuado
@Composable
fun rememberMapStyle(darkTheme: Boolean): MapStyleOptions? {
    val context = LocalContext.current
    return remember(darkTheme) {
        try {
            val resId = if (darkTheme) R.raw.map_style_dark else R.raw.map_style_vintage
            context.resources.openRawResource(resId)
                .bufferedReader()
                .use { MapStyleOptions(it.readText()) }
        } catch (e: Exception) {
            null // Fallback al estilo por defecto
        }
    }
}

// Composable local para el estilo del mapa
val LocalMapStyle = staticCompositionLocalOf<MapStyleOptions?> { null }
