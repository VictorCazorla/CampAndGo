package com.tfg.campandgo.data.model

data class CamperSite(
    val id: String,
    val name: String,
    val formatted_address: String,
    val description: String,
    val mainImageUrl: String,
    val images: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val amenities: List<String>,
    val reviews: List<CamperSiteReview>
)
