package com.tfg.campandgo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ToggleButtonGrid(
    onFilterSelected: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedButtons = remember { mutableStateListOf<String>() }
    var expanded by remember { mutableStateOf(false) }
    val campArray = listOf("campground", "rv_park", "park")

    val buttons = listOf(
        "restaurant" to Icons.Default.Restaurant,
        "lodging" to Icons.Default.Hotel,
        "car_repair" to Icons.Default.Build,
        "gas_station" to Icons.Default.LocalGasStation,
        "supermarket" to Icons.Default.LocalGroceryStore,
        "parking" to Icons.Default.LocalParking,
        "laundry" to Icons.Default.LocalLaundryService,
        "camping" to Icons.Default.Place,
    )

    // Solo IconButton para filtros, alineado con los demás
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filtrar",
                modifier = Modifier.size(28.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            buttons.forEach { (key, icon) ->
                val isSelected = selectedButtons.contains(key)

                DropdownMenuItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else Color.Transparent
                        ),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(key.replace("_", " ").replaceFirstChar { it.uppercase() })
                        }
                    },
                    onClick = {
                        if (isSelected) {
                            if (key == "camping") campArray.forEach { selectedButtons.remove(it) }
                            selectedButtons.remove(key)
                        } else {
                            if (key == "camping") campArray.forEach { selectedButtons.add(it) }
                            selectedButtons.add(key)
                        }
                        onFilterSelected(selectedButtons.toList())
                    }
                )
            }

            DropdownMenuItem(
                text = {
                    Text("Cerrar", color = MaterialTheme.colorScheme.primary)
                },
                onClick = { expanded = false }
            )
        }
    }
}

