package com.tfg.campandgo.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tfg.campandgo.ui.viewmodel.MapsViewModel
import com.tfg.campandgo.ui.component.*

/**
 * Pantalla principal de la aplicación que gestiona la búsqueda de ubicaciones y la visualización del mapa.
 *
 * Esta pantalla es responsable de:
 * - Obtener la API Key del archivo `AndroidManifest.xml`.
 * - Gestionar los permisos de ubicación.
 * - Realizar búsquedas de ubicaciones y mostrar sugerencias.
 * - Mostrar el mapa con la ubicación actual y los lugares cercanos.
 *
 * @param navigator Navegador que permite moverse entre pantallas.
 * @see MapsViewModel Para más detalles sobre el ViewModel utilizado.
 * @see MapScreen Para la pantalla de visualización del mapa.
 * @see ErrorScreen Para mostrar mensajes de error.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navigator: NavController) {
    val viewModel: MapsViewModel = viewModel()
    val context = LocalContext.current
    val apiKey = remember { getApiKeyFromManifest(context) }
    var searchQuery by remember { mutableStateOf("") }

    // Si no se encuentra la API Key, mostrar un mensaje de error y salir
    if (apiKey == null) {
        ErrorScreen(message = "Error: API Key no configurada en el Manifest")
        return
    }

    // Gestionar permisos de ubicación
    PermissionHandler(viewModel)

    // Si se tienen permisos de ubicación, obtener la ubicación actual
    if (viewModel.hasLocationPermission.value) {
        LocationFetcher { location ->
            viewModel.selectedLocation.value = location
        }

        // Realizar búsqueda de ubicaciones cuando el texto de búsqueda tenga más de 2 caracteres
        LaunchedEffect(searchQuery) {
            if (searchQuery.length > 2) {
                viewModel.searchLocations(searchQuery, apiKey, context)
            }
        }

        // Mostrar la pantalla del mapa con los datos actuales
        MapScreen(
            navigator = navigator,
            currentLocation = viewModel.selectedLocation.value,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearch = { placeId -> viewModel.getLocationDetails(placeId, apiKey, context) },
            searchSuggestions = viewModel.searchSuggestions,
            nearbyPlaces = viewModel.nearbyPlaces,
            viewModel = viewModel
        )
    }
}

/**
 * Obtiene la API Key de Google Maps desde el archivo `AndroidManifest.xml`.
 *
 * @param context El contexto de la aplicación.
 * @return La API Key como String, o `null` si no se encuentra o hay un error.
 */
private fun getApiKeyFromManifest(context: Context): String? {
    return try {
        val appInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        appInfo.metaData.getString("com.google.android.geo.API_KEY")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}