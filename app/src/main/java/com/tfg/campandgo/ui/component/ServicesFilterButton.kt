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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tfg.campandgo.R

@Composable
fun ToggleButtonGrid(
    onFilterSelected: (List<String>) -> Unit,
    onNearbySearchToggle: () -> Unit,
    isNearbySearchActive: Boolean
) {
    val selectedButtons = remember { mutableStateListOf<String>() }
    var expanded by remember { mutableStateOf(false) }

    val buttons = listOf(
        "restaurant" to Icons.Default.Restaurant,
        "lodging" to Icons.Default.Hotel,
        "car_repair" to Icons.Default.Build,
        "gas_station" to Icons.Default.LocalGasStation,
        "supermarket" to Icons.Default.LocalGroceryStore,
        "parking" to Icons.Default.LocalParking,
        "laundry" to Icons.Default.LocalLaundryService,
    )

    Box {
        IconButton(
            onClick = { expanded = true },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.find_icon),
                contentDescription = "Filtrar lugares de interés",
                modifier = Modifier.size(28.dp),
                tint = if (selectedButtons.isNotEmpty() || isNearbySearchActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isNearbySearchActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = if (isNearbySearchActive) "Hide places" else "Show places",
                            color = if (isNearbySearchActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                onClick = {
                    onNearbySearchToggle()
                    expanded = false
                }
            )

            Divider()

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
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = key.replace("_", " ").replaceFirstChar { it.uppercase() },
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        if (isSelected) {
                            selectedButtons.remove(key)
                        } else {
                            selectedButtons.add(key)
                        }
                        onFilterSelected(selectedButtons.toList())
                    }
                )
            }

            Divider()

            DropdownMenuItem(
                text = {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                },
                onClick = { expanded = false }
            )
        }
    }
}