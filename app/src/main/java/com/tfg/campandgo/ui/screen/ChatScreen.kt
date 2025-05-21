package com.tfg.campandgo.ui.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.coroutines.cancellation.CancellationException

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
                title = {
                    Text(
                        "Chat del camping",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Message list with gradient effect
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.surface,
                            0.1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    state = scrollState,
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { msg ->
                        MessageBubble(
                            message = msg,
                            isCurrentUser = msg.senderName == userName,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Input area with shadow
            Box(
                modifier = Modifier
                    .shadow(elevation = 8.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
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
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean, modifier: Modifier = Modifier) {
    val bubbleColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val timeColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (!isCurrentUser) {
            Text(
                text = message.senderName ?: "Anónimo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
            )
        }

        Surface(
            color = bubbleColor,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp,
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                message.text.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.None
                            )
                        ),
                        color = textColor
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
                    color = timeColor,
                    modifier = Modifier.align(Alignment.End)
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
    Column(modifier = modifier) {
        imageUri?.let { uri ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium)
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
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Quitar imagen",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onAddImageClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Añadir imagen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = {
                    Text(
                        "Escribe un mensaje...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.large,
                maxLines = 3,
                singleLine = false
            )

            IconButton(
                onClick = onSend,
                enabled = messageText.isNotBlank() || imageUri != null,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (messageText.isNotBlank() || imageUri != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar mensaje",
                    tint = if (messageText.isNotBlank() || imageUri != null) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
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
        val messagesRef = db.collection("chats")
            .document(camperSiteId)
            .collection("messages")

        try {
            val messagesSnapshot = messagesRef
                .orderBy("timestamp")
                .get()
                .await()

            if (messagesSnapshot.size() > 30) {
                val oldMessages = messagesSnapshot.documents.take(1)
                for (doc in oldMessages) {
                    try {
                        doc.reference.delete().await()
                    } catch (_: Exception) { }
                }
            }
        } catch (_: Exception) { }

        val messageMap = mutableMapOf(
            "senderName" to (userName ?: "Anónimo"),
            "timestamp" to Timestamp.now()
        )

        if (messageText.isNotBlank()) {
            messageMap["text"] = messageText
        }

        imageUri?.let { uri ->
            try {
                val imageUrl = uploadImageAndGetUrl(uri)
                messageMap["imageUrl"] = imageUrl
            } catch (_: Exception) {
                if (messageText.isBlank()) {
                    throw Exception("Failed to upload image and no text provided")
                }
            }
        }

        try {
            messagesRef.add(messageMap).await()
            onComplete()
        } catch (e: Exception) {
            throw e
        }

    } catch (e: Exception) {
        if (e is CancellationException) throw e
        throw e
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