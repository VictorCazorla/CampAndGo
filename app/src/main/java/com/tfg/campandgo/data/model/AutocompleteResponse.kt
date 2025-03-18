package com.tfg.campandgo.data.model

data class AutocompleteResponse(
    val predictions: List<Prediction>,
    val status: String
)

data class Prediction(
    val description: String,
    val place_id: String,
    val structured_formatting: StructuredFormatting
)

data class StructuredFormatting(
    val main_text: String,
    val secondary_text: String
)