package com.tfg.campandgo.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.tfg.campandgo.data.model.ChatMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ChatScreen(camperSiteId: String, userName: String?, navigator: NavController) {
    val db = FirebaseFirestore.getInstance()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val scope = rememberCoroutineScope()

    var newMessage by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(camperSiteId) {
        db.collection("chats")
            .document(camperSiteId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) }?.let {
                    messages.clear()
                    messages.addAll(it)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    msg.senderName?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall)
                    }
                    msg.text.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    msg.imageUrl?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Image",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") }
            )

            IconButton(onClick = {
                scope.launch {
                    val messageMap = mutableMapOf(
                        "text" to newMessage,
                        "senderName" to (userName ?: "Anónimo"),
                        "timestamp" to Timestamp.now()
                    )

                    if (imageUri != null) {
                        val fileName = "chat_images/${System.currentTimeMillis()}.jpg"
                        val imageRef = FirebaseStorage.getInstance().reference.child(fileName)
                        imageRef.putFile(imageUri!!).await()
                        val downloadUrl = imageRef.downloadUrl.await()
                        messageMap["imageUrl"] = downloadUrl.toString()
                    }

                    db.collection("chats")
                        .document(camperSiteId)
                        .collection("messages")
                        .add(messageMap)
                        .await()

                    newMessage = ""
                    imageUri = null
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}
