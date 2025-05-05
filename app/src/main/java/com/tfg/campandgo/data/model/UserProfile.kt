package com.tfg.campandgo.data.model

data class UserProfile(
    var userId: String = "",
    var userName: String = "",
    var userDescription: String = "",
    var profileImageUri: String? = null,
    var bannerImageUri: String? = null,
    var visitedSitesCount: Int = 0,
    var reviewsCount: Int = 0,
    var userStory: String = "",
    var tags: List<String> = emptyList(),
)