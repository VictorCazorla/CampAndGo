package com.tfg.campandgo.data.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class CamperSite(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("formatted_address")
    @set:PropertyName("formatted_address")
    var formattedAddress: String = "",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("main_image_url")
    @set:PropertyName("main_image_url")
    var mainImageUrl: String = "",

    @get:PropertyName("images")
    @set:PropertyName("images")
    var images: List<String> = emptyList(),

    @get:PropertyName("videos")
    @set:PropertyName("videos")
    var videos: List<String> = emptyList(),

    @get:PropertyName("rating")
    @set:PropertyName("rating")
    var rating: Double = 0.0,

    @get:PropertyName("review_count")
    @set:PropertyName("review_count")
    var reviewCount: Int = 0,

    @get:PropertyName("amenities")
    @set:PropertyName("amenities")
    var amenities: List<String> = emptyList(),

    @get:PropertyName("location")
    @set:PropertyName("location")
    var location: GeoPoint = GeoPoint(0.0, 0.0)
)
