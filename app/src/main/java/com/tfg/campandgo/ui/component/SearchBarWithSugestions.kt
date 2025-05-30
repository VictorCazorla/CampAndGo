package com.tfg.campandgo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tfg.campandgo.data.model.Prediction

/**
 * Una función composable que representa una barra de búsqueda con sugerencias.
 * Permite al usuario buscar una ubicación, ver sugerencias en base a su consulta,
 * centrar el mapa en la posición actual o realizar una búsqueda explícita.
 *
 * @param searchQuery Cadena de texto que representa la consulta actual en el campo de búsqueda.
 * @param onSearchQueryChange Callback que se ejecuta cuando el usuario modifica la consulta.
 * @param suggestions Lista de objetos `Prediction` que representan las sugerencias basadas en la consulta.
 * @param onSuggestionSelected Callback que se ejecuta cuando el usuario selecciona una sugerencia.
 *                              Recibe el objeto `Prediction` correspondiente.
 * @param onCenterMap Callback que se ejecuta al presionar el botón para centrar el mapa en la ubicación actual.
 * @param onSearch Callback que se ejecuta cuando el usuario presiona el botón de búsqueda.
 * @param modifier Modificador estético.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithSuggestions(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    suggestions: List<Prediction>,
    onSuggestionSelected: (Prediction) -> Unit,
    onCenterMap: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            // Fila de búsqueda
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search here...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .weight(0.2f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                // Botón de centrar mapa
                IconButton(
                    onClick = onCenterMap,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Centrar mapa",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Lista de sugerencias
            if (suggestions.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(max = 220.dp)
                ) {
                    items(suggestions) { prediction ->
                        // Uso de un Card para mejorar la estética de cada sugerencia
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            SuggestionItem(
                                prediction = prediction,
                                onSuggestionSelected = {
                                    onSuggestionSelected(prediction)
                                    onSearch() // Ejecuta la búsqueda después de seleccionar
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


