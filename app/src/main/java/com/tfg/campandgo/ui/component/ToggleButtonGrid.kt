package com.tfg.campandgo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ToggleButtonGrid(
    onFilterSelected: (List<String>) -> Unit
) {
    // Lista mutable para almacenar los nombres de los botones seleccionados
    val selectedButtons = remember { mutableStateListOf<String>() }

    // Lista de botones con sus términos asociados
    val buttons = listOf(
        Pair("restaurant", Icons.Default.Restaurant),
        Pair("lodging", Icons.Default.Hotel),
        Pair("car_repair", Icons.Default.Build),
        Pair("gas_station", Icons.Default.LocalGasStation),
        Pair("supermarket", Icons.Default.LocalGroceryStore),
        Pair("parking", Icons.Default.LocalParking),
        Pair("camping", Icons.Default.Terrain),
        Pair("laundry", Icons.Default.LocalLaundryService),
        Pair("rest_stop", Icons.Default.Place)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        buttons.chunked(3).forEach { rowButtons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowButtons.forEach { (key, icon) ->
                    ToggleButton(
                        icon = icon,
                        isSelected = selectedButtons.contains(key),
                        onClick = {
                            // Añadir o eliminar el nombre del botón de la lista
                            if (selectedButtons.contains(key)) {
                                selectedButtons.remove(key)
                            } else {
                                selectedButtons.add(key)
                            }
                            // Notificar la lista actualizada
                            onFilterSelected(selectedButtons)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ToggleButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.Blue else Color.LightGray
    val contentColor = if (isSelected) Color.White else Color.Black

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .background(backgroundColor, shape = CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
    }
}
