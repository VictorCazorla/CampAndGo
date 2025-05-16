package com.tfg.campandgo.data.model

import com.google.firebase.firestore.GeoPoint

data class CamperSite(
    val id: String,
    val name: String,
    val formattedAddress: String,
    val description: String,
    val mainImageUrl: String,
    val images: List<String>,
    val videos: List<String> = emptyList(),
    val rating: Double,
    val reviewCount: Int,
    val amenities: List<String>,
    val reviews: List<CamperSiteReview>,
    val location: GeoPoint
)
