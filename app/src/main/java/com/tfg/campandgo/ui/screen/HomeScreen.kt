package com.tfg.campandgo.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tfg.campandgo.data.model.CamperSite
import com.tfg.campandgo.data.model.CamperSiteReview
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
 * @see MapsViewModel Para más detalles sobre el ViewModel utilizado.
 * @see MapScreen Para la pantalla de visualización del mapa.
 * @see ErrorScreen Para mostrar mensajes de error.
 */
@Composable
fun HomeScreen() {
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

@Composable
private fun launchCampsite() {
    val sampleCamperSite = CamperSite(
        id = "cs001",
        name = "Paradise Camping",
        formatted_address = "Sierra Nevada, California",
        description = "Un camping espectacular con vistas increíbles a las montañas. Ofrece parcelas espaciosas, zonas de acampada privadas y acceso directo a rutas de senderismo. Instalaciones modernas incluyen baños limpios, duchas con agua caliente y zona de lavandería.",
        mainImageUrl = "https://example.com/campsite1.jpg",
        images = listOf(
            "https://example.com/campsite1.jpg",
            "https://example.com/campsite2.jpg",
            "https://example.com/campsite3.jpg",
            "https://example.com/campsite4.jpg"
        ),
        rating = 4.7,
        reviewCount = 128,
        amenities = listOf(
            "Wifi",
            "Duchas calientes",
            "Lavandería",
            "Tienda",
            "Zona de fogatas",
            "Piscina",
            "Parque infantil",
            "Electricidad",
            "Agua potable",
            "Barbacoa"
        ),
        reviews = listOf(
            CamperSiteReview(
                userName = "María González",
                rating = 5.0,
                date = "15/05/2023",
                comment = "Increíble experiencia familiar. Las instalaciones están impecables y el personal es muy amable. ¡Volveremos seguro!",
                images = listOf("https://example.com/review1.jpg")
            ),
            CamperSiteReview(
                userName = "Carlos Martínez",
                rating = 4.5,
                date = "22/06/2023",
                comment = "Muy buen camping, aunque la señal de wifi podría mejorar en algunas zonas. Las vistas son espectaculares al amanecer."
            ),
            CamperSiteReview(
                userName = "Ana López",
                rating = 4.0,
                date = "03/07/2023",
                comment = "Bonito lugar pero algo caro para lo que ofrece. Las duchas estaban limpias pero a veces no había agua caliente.",
                images = listOf(
                    "https://example.com/review2.jpg",
                    "https://example.com/review3.jpg"
                )
            )
        )
    )

    CamperSiteScreen(
        site = sampleCamperSite,
        onBackClick = { /* Lógica para volver atrás */ },
        onBookClick = { /* Lógica para reservar */ }
    )
}