package com.tfg.campandgo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
 * @param errorMessage Mensaje de error opcional que se muestra si ocurre un problema durante la búsqueda.
 * @param onSuggestionSelected Callback que se ejecuta cuando el usuario selecciona una sugerencia.
 *                              Recibe el objeto `Prediction` correspondiente.
 * @param onCenterMap Callback que se ejecuta al presionar el botón para centrar el mapa en la ubicación actual.
 * @param onSearch Callback que se ejecuta cuando el usuario presiona el botón de búsqueda.
 */
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
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        ) {
            // Fila de búsqueda
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    singleLine = true
                )

                // Botón de búsqueda explícita
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                    )
                }

                // Botón de centrar mapa
                IconButton(
                    onClick = onCenterMap,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Centrar mapa",
                    )
                }
            }

            // Lista de sugerencias
            if (suggestions.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    items(suggestions) { prediction ->
                        SuggestionItem(
                            prediction = prediction,
                            onSuggestionSelected = onSuggestionSelected,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}