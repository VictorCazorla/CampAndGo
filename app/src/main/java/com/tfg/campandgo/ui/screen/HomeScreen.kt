package com.tfg.campandgo.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.data.model.CamperSite
import com.tfg.campandgo.data.model.CamperSiteReview
import com.tfg.campandgo.ui.viewmodel.MapsViewModel
import com.tfg.campandgo.ui.component.*
import kotlinx.coroutines.tasks.await

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
        /*MapScreen(
            currentLocation = viewModel.selectedLocation.value,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearch = { placeId -> viewModel.getLocationDetails(placeId, apiKey, context) },
            searchSuggestions = viewModel.searchSuggestions,
            nearbyPlaces = viewModel.nearbyPlaces,
            viewModel = viewModel
        )*/
        launchCampsite()

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

    var sampleCamperSite by remember { mutableStateOf(
        CamperSite(
            "", "", "", "", "", listOf(""), 0.0, 0, listOf(""), listOf(CamperSiteReview("", 0.0, "", "", listOf())))
    ) }
    val db = Firebase.firestore

    // Versión 1
    /*db.collection("camper_sites")
        .whereEqualTo("id", "long_lat")
        .get()
        .addOnSuccessListener { camperSites ->
            if (camperSites.isEmpty) {
                Log.d("LaunchCampsite", "No camper site found")
            }

            for (camperSite in camperSites) {
                sampleCamperSite = CamperSite(
                    id = camperSite.data["id"].toString(),
                    name = camperSite.data["name"].toString(),
                    formattedAddress = camperSite.data["formatted_address"].toString(),
                    description = camperSite.data["description"].toString(),
                    mainImageUrl = camperSite.data["main_image_url"].toString(),
                    images = (camperSite["images"] as List<*>).map { it as String },
                    rating = camperSite.data["rating"].toString().toDouble(),
                    reviewCount = camperSite.data["review_count"].toString().toInt(),
                    amenities = (camperSite["amenities"] as List<*>).map { it as String },
                    reviews = (camperSite["reviews"] as List<*>).map { it as CamperSiteReview }
                )
            }
        }
        .addOnFailureListener { e ->
            Log.d("MapsViewModel", "Error searching the camper site", e)
        }*/
    // Usar un LaunchedEffect para realizar la consulta solo una vez
    LaunchedEffect(Unit) {
        // Versión 2
        /*db.collection("camper_sites")
            .whereEqualTo("id", "long_lat")
            .get()
            .addOnSuccessListener { camperSites ->
                if (camperSites.isEmpty) {
                    Log.d("MapsViewModel", "No camper site found")
                    return@addOnSuccessListener
                }

                val camperSite = camperSites.first() // Asumiendo que hay al menos un resultado
                sampleCamperSite = CamperSite(
                    id = camperSite.data["id"].toString(),
                    name = camperSite.data["name"].toString(),
                    formattedAddress = camperSite.data["formatted_address"].toString(),
                    description = camperSite.data["description"].toString(),
                    mainImageUrl = camperSite.data["main_image_url"].toString(),
                    images = (camperSite["images"] as List<*>).map { it as String },
                    rating = camperSite.data["rating"].toString().toDouble(),
                    reviewCount = camperSite.data["review_count"].toString().toInt(),
                    amenities = (camperSite["amenities"] as List<*>).map { it as String },
                    reviews = (camperSite["reviews"] as List<*>).map { it as CamperSiteReview }
                )
            }
            .addOnFailureListener { e ->
                Log.d("MapsViewModel", "Error searching the camper site", e)
            }*/

        // Versión 3
        try {
            val camperSitesSnapshot = db.collection("camper_sites")
                .whereEqualTo("id", "long_lat")  // Aquí puedes ajustar el filtro según tus necesidades
                .get()
                .await()  // Espera respuesta de manera síncrona

            if (camperSitesSnapshot.isEmpty) {
                Log.d("LaunchCampsite", "No camper site found")
            } else {
                val camperSite = camperSitesSnapshot.documents.first()
                sampleCamperSite = CamperSite(
                    id = camperSite.getString("id") ?: "",
                    name = camperSite.getString("name") ?: "",
                    formattedAddress = camperSite.getString("formatted_address") ?: "",
                    description = camperSite.getString("description") ?: "",
                    mainImageUrl = camperSite.getString("main_image_url") ?: "",
                    images = camperSite.get("images") as? List<String> ?: listOf(),
                    rating = camperSite.getDouble("rating") ?: 0.0,
                    reviewCount = camperSite.getLong("review_count")?.toInt() ?: 0,
                    amenities = camperSite.get("amenities") as? List<String> ?: listOf(),
                    reviews = (camperSite.get("reviews") as? List<Map<String, Any>>)?.map {
                        CamperSiteReview(
                            userName = it["user_name"] as? String ?: "",
                            rating = (it["rating"] as? Double) ?: 0.0,
                            comment = it["comment"] as? String ?: "",
                            date = it["date"] as? String ?: "",
                            images = it["images"] as? List<String> ?: listOf()
                        )
                    } ?: listOf()
                )
            }
        } catch (e: Exception) {
            Log.e("LaunchCampsite", "Error fetching camper site", e)
        }
    }

    CamperSiteScreen(
        site = sampleCamperSite,
        onBackClick = { /* Lógica para volver atrás */ },
        onBookClick = { /* Lógica para reservar */ }
    )
}