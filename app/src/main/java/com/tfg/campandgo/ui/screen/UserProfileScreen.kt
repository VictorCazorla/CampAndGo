package com.tfg.campandgo.ui.screen

import android.annotation.SuppressLint
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import kotlin.math.min

@SuppressLint("DefaultLocale")
@Composable
fun UserProfileScreen(email: String, navigator: NavController) {
    val db = Firebase.firestore
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Anonymous") }
    var userImage by remember { mutableStateOf("") }
    var bannerImage by remember { mutableStateOf("") }
    var camperHistory by remember { mutableStateOf("About my trips") }
    var tagList by remember { mutableStateOf(emptyList<String>()) }
    var visitedPlaces by remember { mutableIntStateOf(0) }
    var reviews by remember { mutableIntStateOf(0) }
    var favoriteCamperSites by remember { mutableStateOf<List<DocumentReference>>(emptyList()) }
    var visitedCamperSites by remember { mutableStateOf<List<String>>(emptyList()) }

    val favoriteSites = remember { mutableStateListOf<SimpleCamperSite>() }

    var isEditing by remember { mutableStateOf(false) }
    var tempUserName by remember { mutableStateOf("") }
    var tempUserImage by remember { mutableStateOf("") }
    var tempBannerImage by remember { mutableStateOf("") }
    var tempCamperHistory by remember { mutableStateOf("") }
    var tempTagList by remember { mutableStateOf(emptyList<String>()) }
    var tempVisitedPlaces by remember { mutableIntStateOf(0) }
    var tempReviews by remember { mutableIntStateOf(0) }
    var tempFavoriteCamperSites by remember { mutableStateOf<List<DocumentReference>>(emptyList()) }
    var tempVisitedCamperSites by remember { mutableStateOf<List<String>>(emptyList()) }

    // Profile image handling
    val profileCameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val profileImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val downloadUrl = uploadImageToFirebase(it, "profile_images", context)
                downloadUrl?.let { url ->
                    tempUserImage = url
                }
            }
        }
    }

    val profileCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profileCameraImageUri.value?.let { uri ->
                scope.launch {
                    val downloadUrl = uploadImageToFirebase(uri, "profile_images", context)
                    downloadUrl?.let { url ->
                        tempUserImage = url
                    }
                }
            }
        }
    }

    // Banner image handling
    val bannerCameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val bannerGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val downloadUrl = uploadImageToFirebase(it, "banner_images", context)
                downloadUrl?.let { url ->
                    tempBannerImage = url
                }
            }
        }
    }

    val bannerCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            bannerCameraImageUri.value?.let { uri ->
                scope.launch {
                    val downloadUrl = uploadImageToFirebase(uri, "banner_images", context)
                    downloadUrl?.let { url ->
                        tempBannerImage = url
                    }
                }
            }
        }
    }

    // State to display dialogs
    var showProfileImagePickerDialog by remember { mutableStateOf(false) }
    var showBannerImagePickerDialog by remember { mutableStateOf(false) }

    // Load user data
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
                favoriteCamperSites = it["favorite_camper_sites"] as? List<DocumentReference> ?: emptyList()
                visitedCamperSites = it["visited_camper_sites"] as? List<String> ?: emptyList()

                // Initialize temporary values
                tempUserName = userName
                tempUserImage = userImage
                tempBannerImage = bannerImage
                tempCamperHistory = camperHistory
                tempTagList = tagList
                tempVisitedPlaces = visitedPlaces
                tempReviews = reviews
                tempFavoriteCamperSites = favoriteCamperSites
                tempVisitedCamperSites = visitedCamperSites
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Error loading profile: ${e.message}")
            }
        }
    }

    // Load favorite sites
    LaunchedEffect(email) {
        scope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(email)
                    .get()
                    .await()

                val favoriteRefs = userDoc.get("favorite_camper_sites") as? List<DocumentReference> ?: emptyList()

                favoriteSites.clear()
                favoriteRefs.forEach { docRef ->
                    val siteSnapshot = docRef.get().await()
                    val siteID = siteSnapshot.getString("id") ?: ""
                    val siteName = siteSnapshot.getString("name") ?: "Unknown name"
                    val siteRating = siteSnapshot.getDouble("rating") ?: 0.0

                    favoriteSites.add(SimpleCamperSite(siteID, siteName, siteRating))
                }
            } catch (e: Exception) {
                Log.e("FavoriteSites", "Error fetching favorite sites", e)
            }
        }
    }

    // Remove from favorites function
    suspend fun removeFromFavorites(email: String, siteId: String) {
        try {
            val userRef = db.collection("users").document(email)

            db.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val currentFavorites = userDoc.get("favorite_camper_sites") as? List<DocumentReference> ?: emptyList()

                val updatedFavorites = currentFavorites.filter { it.id != siteId }

                transaction.update(userRef, "favorite_camper_sites", updatedFavorites)
            }.await()
        } catch (e: Exception) {
            Log.e("FavoriteSites", "Error removing favorite", e)
        }
    }

    // Save changes function
    fun saveChanges() {
        scope.launch {
            try {
                val updates = mapOf(
                    "user_name" to tempUserName.ifEmpty { "Anonymous" },
                    "user_image" to tempUserImage.ifEmpty { userImage },
                    "banner_image" to tempBannerImage.ifEmpty { bannerImage },
                    "camper_history" to tempCamperHistory,
                    "tag_list" to tempTagList,
                    "visited_places" to tempVisitedPlaces,
                    "reviews" to tempReviews,
                    "favorite_camper_sites" to tempFavoriteCamperSites,
                    "visited_camper_sites" to tempVisitedCamperSites,
                    "email" to email
                )

                db.collection("users").document(email).set(updates).await()

                // Update all states
                userName = tempUserName
                userImage = tempUserImage.ifEmpty { userImage }
                bannerImage = tempBannerImage.ifEmpty { bannerImage }
                camperHistory = tempCamperHistory
                tagList = tempTagList
                visitedPlaces = tempVisitedPlaces
                reviews = tempReviews
                favoriteCamperSites = tempFavoriteCamperSites
                visitedCamperSites = tempVisitedCamperSites

                isEditing = false
                snackbarHostState.showSnackbar("Profile updated successfully")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error updating profile: ${e.message}")
            }
        }
    }

    // Achievements Section
    val achievements = remember(visitedPlaces, reviews) {
        listOf(
            // Achievements based on places visited
            AchievementData(
                icon = Icons.Default.Place,
                title = "First steps",
                description = "Visit your first place",
                current = minOf(visitedPlaces, 1),
                target = 1,
                color = Color(0xFF0A940F)
            ),
            AchievementData(
                icon = Icons.Default.Explore,
                title = "Novice explorer",
                description = "Visit 5 places",
                current = minOf(visitedPlaces, 5),
                target = 5,
                color = Color(0xFF8A61D5)
            ),
            AchievementData(
                icon = Icons.Default.TravelExplore,
                title = "Experienced traveler",
                description = "Visit 15 places",
                current = minOf(visitedPlaces, 15),
                target = 15,
                color = Color(0xE6D770D7)
            ),
            AchievementData(
                icon = Icons.Default.Flag,
                title = "Scout Master",
                description = "Visit 30 places",
                current = minOf(visitedPlaces, 30),
                target = 30,
                color = Color(0xFF388E3C)
            ),
            AchievementData(
                icon = Icons.Default.Public,
                title = "Camper legend",
                description = "Visit 50 places",
                current = minOf(visitedPlaces, 50),
                target = 50,
                color = Color(0xFFFFA000)
            ),

            // Review based on achievements
            AchievementData(
                icon = Icons.Default.StarOutline,
                title = "First review",
                description = "Write your first review",
                current = minOf(reviews, 1),
                target = 1,
                color = Color(0xFF00BCD4)
            ),
            AchievementData(
                icon = Icons.Default.StarHalf,
                title = "Beginner critic",
                description = "Write 5 reviews",
                current = minOf(reviews, 5),
                target = 5,
                color = Color(0xFF673AB7)
            ),
            AchievementData(
                icon = Icons.Default.Star,
                title = "Review Expert",
                description = "Write 20 reviews",
                current = minOf(reviews, 20),
                target = 20,
                color = Color(0xFFFFC107)
            ),
            AchievementData(
                icon = Icons.Default.AutoAwesome,
                title = "Review Guru",
                description = "Write 50 reviews",
                current = minOf(reviews, 50),
                target = 50,
                color = Color(0xFFE91E63)
            ),

            // Combined achievements
            AchievementData(
                icon = Icons.Default.ThumbsUpDown,
                title = "Perfect balance",
                description = "10 places + 10 reviews",
                current = minOf(min(visitedPlaces, reviews), 10),
                target = 10,
                color = Color(0xFF9C27B0)
            ),
            AchievementData(
                icon = Icons.Default.LocalActivity,
                title = "Camper Ambassador",
                description = "25 places + 25 reviews",
                current = minOf(min(visitedPlaces, reviews), 25),
                target = 25,
                color = Color(0xFF3F51B5)
            ),
            AchievementData(
                icon = Icons.Default.Stars,
                title = "Total legend",
                description = "50 places + 50 reviews",
                current = minOf(min(visitedPlaces, reviews), 50),
                target = 50,
                color = Color(0xFFFF5722)
            )
        ).filter { it.target > 0 }
    }

    Scaffold(
    ) { paddingValues ->
        // Profile image picker dialog
        if (showProfileImagePickerDialog) {
            AlertDialog(
                onDismissRequest = { showProfileImagePickerDialog = false },
                title = { Text("Select profile image") },
                text = {
                    Column {
                        Text("Take a photo with camera",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val uri = createImageFile(context)
                                    profileCameraImageUri.value = uri
                                    profileCameraLauncher.launch(uri)
                                    showProfileImagePickerDialog = false
                                }
                                .padding(16.dp))

                        Text("Choose from gallery",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    profileImageLauncher.launch("image/*")
                                    showProfileImagePickerDialog = false
                                }
                                .padding(16.dp))
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProfileImagePickerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Banner image picker dialog
        if (showBannerImagePickerDialog) {
            AlertDialog(
                onDismissRequest = { showBannerImagePickerDialog = false },
                title = { Text("Select banner image") },
                text = {
                    Column {
                        Text("Take a photo with camera",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val uri = createImageFile(context)
                                    bannerCameraImageUri.value = uri
                                    bannerCameraLauncher.launch(uri)
                                    showBannerImagePickerDialog = false
                                }
                                .padding(16.dp))

                        Text("Choose from gallery",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    bannerGalleryLauncher.launch("image/*")
                                    showBannerImagePickerDialog = false
                                }
                                .padding(16.dp))
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBannerImagePickerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

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
                        model = if (isEditing) {
                            if (tempBannerImage.isNotEmpty()) tempBannerImage
                            else bannerImage.ifEmpty { "https://example.com/default_banner.jpg" }
                        } else {
                            bannerImage.ifEmpty { "https://example.com/default_banner.jpg" }
                        },
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = isEditing) {
                                showBannerImagePickerDialog = true
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

                            AsyncImage(
                                model = if (isEditing) {
                                    tempUserImage.ifEmpty { userImage.ifEmpty { "https://example.com/default_profile.jpg" } }
                                } else {
                                    userImage.ifEmpty { "https://example.com/default_profile.jpg" }
                                },
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(4.dp, Color.White, CircleShape)
                                    .clickable(enabled = isEditing) {
                                        showProfileImagePickerDialog = true
                                    }
                            )
                        }
                    }
                }
            }

            // Name and email section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isEditing) {
                        BasicTextField(
                            value = tempUserName,
                            onValueChange = { tempUserName = it },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(0.8f)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp))
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }


            // Tags section
            item {
                if (isEditing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Existing tags
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            tempTagList.forEach { tag ->
                                Chip(
                                    label = tag,
                                    onDelete = {
                                        tempTagList = tempTagList - tag
                                    },
                                    color = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                        }

                        // Button to add new tags
                        var expanded by remember { mutableStateOf(false) }
                        val suggestedTags = listOf("Camping", "Hiking", "Mountain", "Beach",
                            "Adventure", "Family", "Backpacking", "Nature", "Minimalist")

                        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                            IconButton(
                                onClick = { expanded = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add tag",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                suggestedTags
                                    .filter { it !in tempTagList }
                                    .forEach { tag ->
                                        DropdownMenuItem(
                                            text = { Text(tag) },
                                            onClick = {
                                                tempTagList = tempTagList + tag
                                                expanded = false
                                            }
                                        )
                                    }
                            }
                        }
                    }
                } else if (tagList.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        tagList.forEach { tag ->
                            Chip(
                                label = tag,
                                onDelete = null,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Camper Story section
            item {
                if (camperHistory.isNotEmpty() || isEditing) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "About me",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (isEditing) {
                            BasicTextField(
                                value = tempCamperHistory,
                                onValueChange = { tempCamperHistory = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(8.dp)
                                    .height(100.dp)
                            )
                        } else {
                            Text(
                                text = camperHistory,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            // Statistics
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    // Visited places box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Places visited",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = visitedPlaces.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                text = "Places",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }

                    // Reviews box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Reviews",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = reviews.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                            Text(
                                text = "Reviews",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                }
            }

            // Favorite camper sites
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp)
                        .padding(bottom = 16.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Favorite camper sites",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (favoriteSites.isEmpty()) {
                            Text(
                                text = "There are no favorite campsites yet",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(favoriteSites) { site ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Contenido clickable
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    navigator.navigate("camper_site/${site.id}")
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = site.name,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }

                                        // Rating y botón de eliminar
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            // Rating
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(end = 16.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Rating",
                                                    tint = Color(0xFFFFC107),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = String.format("%.1f", site.rating),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(start = 4.dp)
                                                )
                                            }

                                            // Botón de eliminar
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        removeFromFavorites(email, site.id)
                                                        favoriteSites.removeAll { it.id == site.id }
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove from favorites",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                    Divider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = Color.LightGray.copy(alpha = 0.3f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Achievements title
            item {
                Text(
                    text = "My Achievements (${achievements.count { it.current >= it.target }}/${achievements.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                )
            }

            // Achievements items
            items(achievements) { achievement ->
                AchievementItem(
                    icon = achievement.icon,
                    title = achievement.title,
                    description = achievement.description,
                    currentValue = achievement.current,
                    maxValue = achievement.target,
                    color = achievement.color,
                    isCompleted = achievement.current >= achievement.target
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    SnackbarHost(
        hostState = snackbarHostState
    )
}

/**
 * Simplified CamperSite object.
 */
data class SimpleCamperSite(
    val id: String,
    val name: String,
    val rating: Double
)

/**
 * AchievementData object.
 */
data class AchievementData(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val current: Int,
    val target: Int,
    val color: Color
)

@Composable
fun AchievementItem(
    icon: ImageVector,
    title: String,
    description: String,
    currentValue: Int,
    maxValue: Int,
    color: Color,
    isCompleted: Boolean
) {
    val progress = (currentValue.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = if (isCompleted) 0.3f else 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isCompleted) color else color.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) color
                            else MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = if (isCompleted) 0.8f else 0.6f)
                        )
                    )
                }

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )

            Text(
                text = if (isCompleted) "¡Completed!"
                else "$currentValue/$maxValue (${(progress * 100).toInt()}%)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isCompleted) color
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Composable que representa una etiqueta visual (chip) con estilo redondeado.
 *
 * Este componente se utiliza habitualmente para mostrar filtros, categorías o etiquetas
 * que el usuario puede eliminar si se proporciona una función de callback.
 *
 * Características:
 * - Muestra un texto representativo.
 * - Opción de incluir un botón de eliminación si se define `onDelete`.
 * - Personalización de color de fondo mediante el parámetro `color`.
 *
 * @param label Texto que se mostrará en el chip.
 * @param onDelete Función opcional que se ejecutará al pulsar el icono de cerrar. Si es `null`, no se muestra el botón.
 * @param color Color de fondo del chip.
 */
@Composable
fun Chip(
    label: String,
    onDelete: (() -> Unit)?,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )
        )

        if (onDelete != null) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete tag",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Creates a temporary file for camera images
 */
fun createImageFile(context: Context): Uri {
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

/**
 * Uploads an image to Firebase Storage from a Uri
 */
suspend fun uploadImageToFirebase(uri: Uri, folder: String, context: Context): String? {
    return try {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("$folder/${UUID.randomUUID()}.jpg")

        // Upload the file to Firebase
        val uploadTask = imageRef.putFile(uri).await()

        // Get the download URL
        val downloadUrl = imageRef.downloadUrl.await()

        downloadUrl.toString().also { url ->
            if (url.isEmpty()) {
                throw Exception("Empty download URL")
            }
        }
    } catch (e: Exception) {
        Log.e("UploadImage", "Error uploading image", e)
        Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_LONG).show()
        null
    }
}
