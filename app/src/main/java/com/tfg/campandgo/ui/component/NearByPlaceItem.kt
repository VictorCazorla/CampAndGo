package com.tfg.campandgo.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tfg.campandgo.data.model.Place

@Composable
fun NearbyPlaceItem(place: Place, onPlaceSelected: (Place) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onPlaceSelected(place) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = place.name, style = MaterialTheme.typography.titleMedium)
            Text(text = place.vicinity ?: "Dirección no disponible", style = MaterialTheme.typography.bodySmall)
            Text(text = "Rating: ${place.rating ?: "No disponible"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}