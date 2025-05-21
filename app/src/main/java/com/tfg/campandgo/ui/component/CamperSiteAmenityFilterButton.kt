import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.runtime.State
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tfg.campandgo.R


@Composable
fun CamperSiteAmenityFilterButton(
    selectedAmenities: State<Set<String>>,
    onAmenityToggle: (String) -> Unit,
    onToggleFirebaseSearch: () -> Unit,
    showFirebasePlaces: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    val amenities = listOf(
        "Pernocta" to Icons.Default.NightsStay,
        "Wifi" to Icons.Default.Wifi,
        "Agua potable" to Icons.Default.WaterDrop,
        "Electricidad" to Icons.Default.Bolt,
        "Duchas" to Icons.Default.Shower,
        "Lavandería" to Icons.Default.LocalLaundryService,
        "Baños" to Icons.Default.Wc,
        "Zona de picnic" to Icons.Default.Park,
        "Tienda" to Icons.Default.Store,
        "Restaurante" to Icons.Default.Restaurant,
        "Recepción 24h" to Icons.Default.AccessTime,
    )

    Box {
        IconButton(
            onClick = { expanded = true },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.camping_icon),
                contentDescription = "Filtrar sitios camper",
                modifier = Modifier.size(28.dp),
                tint = if (selectedAmenities.value.isNotEmpty() || selectedAmenities.value.isNotEmpty() || showFirebasePlaces) {
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
            // Filtros de amenities
            amenities.forEach { (label, icon) ->
                val isSelected = selectedAmenities.value.contains(label)

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
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        onAmenityToggle(label)
                    }
                )
            }

            Divider()

            // Opción para mostrar/ocultar campamentos
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
                            tint = if (showFirebasePlaces) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = if (showFirebasePlaces) "Ocultar sitios" else "Mostrar sitios",
                            color = if (showFirebasePlaces) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                onClick = {
                    onToggleFirebaseSearch()
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Cerrar", color = MaterialTheme.colorScheme.primary)
                },
                onClick = { expanded = false }
            )

        }
    }
}