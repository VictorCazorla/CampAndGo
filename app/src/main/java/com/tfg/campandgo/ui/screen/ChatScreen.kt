package com.tfg.campandgo.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.tfg.campandgo.R
import com.tfg.campandgo.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(camperSiteId: String, userName: String?, navigator: NavController) {
    val db = FirebaseFirestore.getInstance()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    var newMessage by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { imageUri = it } }
    )

    // Load messages
    LaunchedEffect(camperSiteId) {
        db.collection("chats")
            .document(camperSiteId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) }?.let {
                    messages.clear()
                    messages.addAll(it)
                    if (messages.isNotEmpty()) {
                        scope.launch {
                            scrollState.animateScrollToItem(0)
                        }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chat del camping", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                state = scrollState,
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    MessageBubble(
                        message = msg,
                        isCurrentUser = msg.senderName == userName,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Input area
            MessageInputField(
                messageText = newMessage,
                onMessageChange = { newMessage = it },
                imageUri = imageUri,
                onImageChange = { imageUri = it },
                onSend = {
                    if (newMessage.isNotBlank() || imageUri != null) {
                        scope.launch {
                            sendMessage(
                                db = db,
                                camperSiteId = camperSiteId,
                                userName = userName,
                                messageText = newMessage,
                                imageUri = imageUri,
                                onComplete = {
                                    newMessage = ""
                                    imageUri = null
                                }
                            )
                        }
                    }
                },
                onAddImageClick = { launcher.launch("image/*") },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean, modifier: Modifier = Modifier) {
    val bubbleColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val shape = if (isCurrentUser) {
        MaterialTheme.shapes.large.copy(
            topEnd = MaterialTheme.shapes.small.topStart,
            bottomStart = MaterialTheme.shapes.large.bottomStart,
            bottomEnd = MaterialTheme.shapes.large.bottomEnd
        )
    } else {
        MaterialTheme.shapes.large.copy(
            topStart = MaterialTheme.shapes.small.topStart,
            bottomStart = MaterialTheme.shapes.large.bottomStart,
            bottomEnd = MaterialTheme.shapes.large.bottomEnd
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!isCurrentUser) {
            Text(
                text = message.senderName ?: "Anónimo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Surface(
            color = bubbleColor,
            shape = shape,
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                message.text.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrentUser) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                message.imageUrl?.let { imageUrl ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen del mensaje",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp?.toDate()?.formatAsTime() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrentUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputField(
    messageText: String,
    onMessageChange: (String) -> Unit,
    imageUri: Uri?,
    onImageChange: (Uri?) -> Unit,
    onSend: () -> Unit,
    onAddImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            imageUri?.let { uri ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Imagen a enviar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.medium)
                    )

                    IconButton(
                        onClick = { onImageChange(null) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar imagen"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAddImageClick) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Añadir imagen"
                    )
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = onSend,
                    enabled = messageText.isNotBlank() || imageUri != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar mensaje"
                    )
                }
            }
        }
    }
}

private suspend fun sendMessage(
    db: FirebaseFirestore,
    camperSiteId: String,
    userName: String?,
    messageText: String,
    imageUri: Uri?,
    onComplete: () -> Unit
) {
    try {
        val messageMap = mutableMapOf(
            "senderName" to (userName ?: "Anónimo"),
            "timestamp" to Timestamp.now()
        )

        if (messageText.isNotBlank()) {
            messageMap["text"] = messageText
        }

        if (imageUri != null) {
            val imageUrl = uploadImageAndGetUrl(imageUri)
            messageMap["imageUrl"] = imageUrl
        }

        db.collection("chats")
            .document(camperSiteId)
            .collection("messages")
            .add(messageMap)
            .await()

        onComplete()
    } catch (e: Exception) {
        // Handle error appropriately
        e.printStackTrace()
    }
}

private suspend fun uploadImageAndGetUrl(uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("chat_images/${System.currentTimeMillis()}.jpg")
        imageRef.putFile(uri).await()
        imageRef.downloadUrl.await().toString()
    }
}

private fun Date.formatAsTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)
}