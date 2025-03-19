package com.tfg.campandgo.data.model

/**
 * Clase de datos que representa la respuesta de una solicitud de autocompletado.
 *
 * @property predictions Lista de objetos `Prediction` que contienen las sugerencias basadas en la entrada del usuario.
 * @property status Un String que indica el estado de la respuesta (por ejemplo, "OK" o "ERROR").
 */
data class AutocompleteResponse(
    val predictions: List<Prediction>,
    val status: String
)

/**
 * Clase de datos que representa una predicción individual en la respuesta de autocompletado.
 *
 * @property description Una descripción general de la predicción, generalmente la dirección o el nombre del lugar.
 * @property place_id Un identificador único para el lugar asociado con la predicción.
 * @property structured_formatting Un objeto `StructuredFormatting` que contiene información estructurada de la predicción.
 */
data class Prediction(
    val description: String,
    val place_id: String,
    val structured_formatting: StructuredFormatting
)

/**
 * Clase de datos que contiene el formato estructurado de una predicción.
 *
 * @property main_text El texto principal de la predicción, como el nombre destacado del lugar.
 * @property secondary_text El texto secundario de la predicción, que generalmente incluye detalles adicionales como la dirección.
 */
data class StructuredFormatting(
    val main_text: String,
    val secondary_text: String
)
