package com.tfg.campandgo.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val text: String = "",
    val senderName: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val imageUrl: String? = null
)
