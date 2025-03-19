package com.tfg.campandgo.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tfg.campandgo.data.model.Prediction

/**
 * Una función composable que representa un elemento de sugerencia.
 * Muestra información de una predicción, incluyendo el texto principal y el texto secundario.
 * También permite al usuario seleccionar la sugerencia a través de un botón.
 *
 * @param prediction Objeto de tipo `Prediction` que contiene la información de la sugerencia a mostrar.
 * @param onSuggestionSelected Callback que se ejecuta cuando el usuario selecciona una sugerencia.
 * Devuelve el objeto `Prediction` correspondiente.
 */
@Composable
fun SuggestionItem(
    prediction: Prediction,
    onSuggestionSelected: (Prediction) -> Unit
) {
    TextButton(
        onClick = { onSuggestionSelected(prediction) },
        modifier = Modifier.fillMaxWidth() // Botón que ocupa el ancho completo del contenedor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp) // Espaciado alrededor del contenido
        ) {
            // Contenedor principal para los textos
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start // Alinea los textos al inicio
            ) {

                Text(
                    text = prediction.structured_formatting.main_text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = prediction.structured_formatting.secondary_text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
