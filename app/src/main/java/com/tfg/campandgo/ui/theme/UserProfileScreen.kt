package com.tfg.campandgo.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.min

@Composable
fun UserProfileScreen(email: String, navigator: NavController) {
    val db = Firebase.firestore
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Anonymous") }
    var userImage by remember { mutableStateOf("") }
    var bannerImage by remember { mutableStateOf("") }
    var camperHistory by remember { mutableStateOf("Sobre mis viajes") }
    var tagList by remember { mutableStateOf(emptyList<String>()) }
    var visitedPlaces by remember { mutableIntStateOf(0) }
    var reviews by remember { mutableIntStateOf(0) }

    var isEditing by remember { mutableStateOf(false) }
    var tempUserName by remember { mutableStateOf("") }
    var tempUserImage by remember { mutableStateOf("") }
    var tempBannerImage by remember { mutableStateOf("") }
    var tempCamperHistory by remember { mutableStateOf("") }
    var tempTagList by remember { mutableStateOf(emptyList<String>()) }
    var tempVisitedPlaces by remember { mutableIntStateOf(0) }
    var tempReviews by remember { mutableIntStateOf(0) }

    // For image picking
    val profileImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val downloadUrl = uploadToFirebase(it, "profile_images", context)
                downloadUrl?.let { url ->
                    tempUserImage = url
                }
            }
        }
    }

    val bannerImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val downloadUrl = uploadToFirebase(it, "banner_images", context)
                downloadUrl?.let { url ->
                    tempBannerImage = url
                }
            }
        }
    }

    LaunchedEffect(email) {
        try {
            val snapshot = db.collection("users").document(email).get().await()
            val data = snapshot.data

            data?.let {
                userName = it["user_name"] as? String ?: ""
                userImage = it["user_image"] as? String ?: ""
                bannerImage = it["banner_image"] as? String ?: ""
                camperHistory = it["camper_history"] as? String ?: ""
                tagList = it["tag_list"] as? List<String> ?: emptyList()
                visitedPlaces = (it["visited_places"] as? Long)?.toInt() ?: 0
                reviews = (it["reviews"] as? Long)?.toInt() ?: 0

                // Initialize temporary values
                tempUserName = userName
                tempUserImage = userImage
                tempBannerImage = bannerImage
                tempCamperHistory = camperHistory
                tempTagList = tagList
                tempVisitedPlaces = visitedPlaces
                tempReviews = reviews
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Error al cargar el perfil: ${e.message}")
            }
        }
    }

    // Function to save changes
    fun saveChanges() {
        scope.launch {
            try {
                val updates = mapOf(
                    "user_name" to tempUserName,
                    "user_image" to tempUserImage,
                    "banner_image" to tempBannerImage,
                    "camper_history" to tempCamperHistory,
                    "tag_list" to tempTagList,
                    "visited_places" to tempVisitedPlaces,
                    "reviews" to tempReviews,
                    "email" to email
                )

                val snapshot = db.collection("users").document(email).get().await()
                if (!snapshot.exists()) {
                    db.collection("users").document(email).set(updates).await()
                } else {
                    db.collection("users").document(email).update(updates).await()
                }

                // Update main values
                userName = tempUserName
                userImage = tempUserImage
                bannerImage = tempBannerImage
                camperHistory = tempCamperHistory
                tagList = tempTagList
                visitedPlaces = tempVisitedPlaces
                reviews = tempReviews

                isEditing = false
                snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al actualizar el perfil: ${e.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Banner section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    AsyncImage(
                        model = if (isEditing) tempBannerImage.ifEmpty { "https://example.com/default_banner.jpg" }
                        else bannerImage.ifEmpty { "https://example.com/default_banner.jpg" },
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = isEditing) {
                                bannerImageLauncher.launch("image/*")
                            }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { navigator.popBackStack() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        if (isEditing) {
                            Row {
                                IconButton(
                                    onClick = {
                                        isEditing = false
                                        // Restore original values
                                        tempUserName = userName
                                        tempUserImage = userImage
                                        tempBannerImage = bannerImage
                                        tempCamperHistory = camperHistory
                                        tempTagList = tagList
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { saveChanges() },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Save",
                                        tint = Color.White
                                    )
                                }
                            }
                        } else {
                            IconButton(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Profile picture and name section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 55.dp) // To overlap with banner
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )

                            // Profile picture
                            AsyncImage(
                                model = if (isEditing) tempUserImage.ifEmpty { "https://example.com/default_profile.jpg" }
                                else userImage.ifEmpty { "https://example.com/default_profile.jpg" },
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(4.dp, Color.White, CircleShape)
                                    .clickable(enabled = isEditing) {
                                        profileImageLauncher.launch("image/*")
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isEditing) {
                            BasicTextField(
                                value = tempUserName,
                                onValueChange = { tempUserName = it },
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth(),
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        } else {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Stats section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        count = if (isEditing) tempVisitedPlaces else visitedPlaces,
                        label = "Lugares visitados",
                        icon = Icons.Default.Place,
                        isEditing = isEditing,
                        onCountChange = { tempVisitedPlaces = it }
                    )
                    StatItem(
                        count = if (isEditing) tempReviews else reviews,
                        label = "Reseñas",
                        icon = Icons.Default.Star,
                        isEditing = isEditing,
                        onCountChange = { tempReviews = it }
                    )
                }
            }

            // Tags section
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Intereses",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (isEditing) {
                        // Add tag input field
                        var newTag by remember { mutableStateOf("") }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = newTag,
                                onValueChange = { newTag = it },
                                label = { Text("Añadir interés") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (newTag.isNotBlank()) {
                                        tempTagList = tempTagList + newTag
                                        newTag = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add tag")
                            }
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (if (isEditing) tempTagList else tagList).forEach { tag ->
                            (if (isEditing) ChipDefaults.chipBorder() else null)?.let {
                                Chip(
                                    onClick = {
                                        if (isEditing) {
                                            tempTagList = tempTagList - tag
                                        }
                                    },
                                    colors = ChipDefaults.chipColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                                    border = it,
                                    label = { Text(tag) },
                                )
                            }
                        }
                    }
                }
            }

            // Bio section
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sobre mí",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (isEditing) {
                        OutlinedTextField(
                            value = tempCamperHistory,
                            onValueChange = { tempCamperHistory = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Cuéntanos sobre tus viajes") },
                            maxLines = 4
                        )
                    } else {
                        Text(
                            text = camperHistory,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    count: Int,
    label: String,
    icon: ImageVector,
    isEditing: Boolean,
    onCountChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )

        if (isEditing) {
            OutlinedTextField(
                value = count.toString(),
                onValueChange = {
                    val newValue = it.toIntOrNull() ?: 0
                    onCountChange(newValue)
                },
                modifier = Modifier.width(80.dp),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    textAlign = TextAlign.Center
                )
            )
        } else {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

suspend fun uploadToFirebase(uri: Uri, folder: String, context: Context): String? {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("$folder/${UUID.randomUUID()}.jpg")

    return try {
        val uploadTask = imageRef.putFile(uri).await()
        imageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
        }
        null
    }
}
