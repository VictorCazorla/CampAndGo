package com.tfg.campandgo.data.model

data class CamperSiteReview(
    val userName: String = "",
    val userImage: String = "",
    val rating: Double = 0.0,
    val date: String = "",
    val comment: String = "",
    val images: List<String> = emptyList()
)
