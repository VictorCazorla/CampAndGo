package com.tfg.campandgo.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.data.model.UserProfile

class UserProfileRepository {
    private val db = Firebase.firestore
    private val userProfilesCollection = db.collection("userProfiles")

    // Obtener perfil del usuario
    fun getUserProfile(userId: String, onSuccess: (UserProfile) -> Unit, onFailure: (Exception) -> Unit) {
        userProfilesCollection.document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess(document.toObject(UserProfile::class.java)!!)
                } else {
                    // Crear un perfil nuevo si no existe
                    val newProfile = UserProfile(userId = userId)
                    userProfilesCollection.document(userId)
                        .set(newProfile)
                        .addOnSuccessListener { onSuccess(newProfile) }
                        .addOnFailureListener(onFailure)
                }
            }
            .addOnFailureListener(onFailure)
    }

    // Actualizar perfil del usuario
    fun updateUserProfile(userId: String, updates: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        userProfilesCollection.document(userId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }
}