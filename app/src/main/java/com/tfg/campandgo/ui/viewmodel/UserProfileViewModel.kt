package com.tfg.campandgo.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.tfg.campandgo.data.model.UserProfile
import com.tfg.campandgo.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserProfileViewModel(
    private val repository: UserProfileRepository = UserProfileRepository()
) : ViewModel() {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadUserProfile()
    }

    fun loadUserProfile(userId: String = "currentUserId") {
        _isLoading.value = true
        repository.getUserProfile(
            userId = userId,
            onSuccess = { profile ->
                _userProfile.value = profile
                _isLoading.value = false
            },
            onFailure = { error ->
                // Manejar error
                _isLoading.value = false
            }
        )
    }

    fun updateProfile(updates: Map<String, Any>) {
        repository.updateUserProfile(
            userId = _userProfile.value.userId,
            updates = updates,
            onSuccess = {
                // Actualizar el estado local con los cambios
                val updatedProfile = _userProfile.value.copy().apply {
                    updates.forEach { (key, value) ->
                        when (key) {
                            "userName" -> userName = value as String
                            "userDescription" -> userDescription = value as String
                            "profileImageUri" -> profileImageUri = value as? String
                            "bannerImageUri" -> bannerImageUri = value as? String
                            "visitedSitesCount" -> visitedSitesCount = value as Int
                            "reviewsCount" -> reviewsCount = value as Int
                            "userStory" -> userStory = value as String
                            "tags" -> tags = value as List<String>
                        }
                    }
                }
                _userProfile.value = updatedProfile
            },
            onFailure = { error ->
                // Manejar error
            }
        )
    }
}