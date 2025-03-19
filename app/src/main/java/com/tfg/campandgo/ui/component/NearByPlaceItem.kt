package com.tfg.campandgo.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tfg.campandgo.data.model.Place

/**
 * Una función composable que representa un elemento de lugar cercano.
 * Este elemento muestra información sobre un lugar, incluyendo su nombre, dirección (si está disponible)
 * y su calificación (si está disponible). Además, permite al usuario interactuar seleccionando el lugar.
 *
 * @param place Objeto de tipo `Place` que contiene la información del lugar cercano a mostrar.
 * @param onPlaceSelected Callback que se ejecuta cuando el usuario selecciona el elemento del lugar.
 *                         Devuelve el objeto `Place` correspondiente.
 */
@Composable
fun NearbyPlaceItem(place: Place, onPlaceSelected: (Place) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth() // Ocupa todo el ancho disponible
            .padding(8.dp) // Espaciado alrededor de la tarjeta
            .clickable { onPlaceSelected(place) }, // Acción al hacer clic
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Elevación de la tarjeta
    ) {
        // Contenedor principal para el contenido del lugar
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = place.vicinity ?: "Dirección no disponible",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Rating: ${place.rating ?: "No disponible"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
