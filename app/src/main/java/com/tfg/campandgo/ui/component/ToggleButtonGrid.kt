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
        Pair("laundry", Icons.Default.LocalLaundryService),
        Pair("rest_stop", Icons.Default.Place),
    )

    val campArray = listOf("campground","rv_park","park")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end=16.dp, top=55.dp) // Añade padding solo a la izquierda
    ) {
        buttons.forEach { (key, icon) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                ToggleButton(
                    icon = icon,
                    isSelected = selectedButtons.contains(key),
                    onClick = {
                        if (selectedButtons.contains(key)) {
                            if(key == "camping") {
                                campArray.forEach { selectedButtons.remove(it) }
                            }
                            selectedButtons.remove(key)
                        } else {
                            if(key == "camping") {
                                campArray.forEach { selectedButtons.add(it) }
                            }
                            selectedButtons.add(key)
                        }
                        onFilterSelected(selectedButtons)
                    },
                )
            }
        }
    }
}

@Composable
fun ToggleButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color.Cyan else Color.LightGray
    val contentColor = if (isSelected) Color.White else Color.Black

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .background(backgroundColor, shape = CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
    }
}
