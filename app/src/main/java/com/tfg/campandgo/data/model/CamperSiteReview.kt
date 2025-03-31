package com.tfg.campandgo.data.model

data class CamperSiteReview(
    val userName: String,
    val rating: Double,
    val date: String,
    val comment: String,
    val images: List<String> = emptyList()
)
