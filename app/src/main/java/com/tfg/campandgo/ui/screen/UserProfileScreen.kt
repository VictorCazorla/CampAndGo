package com.tfg.campandgo.ui.screen

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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.min

@Composable
fun UserProfileScreen(userProfileId: String, isEditable: Boolean = true) {
    val db = Firebase.firestore
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var userName by remember { mutableStateOf("") }
    var userImage by remember { mutableStateOf("") }
    var bannerImage by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var camperHistory by remember { mutableStateOf("") }
    var tagList by remember { mutableStateOf(emptyList<String>()) }
    var visitedPlaces by remember { mutableStateOf(0) }
    var reviews by remember { mutableStateOf(0) }

    // Estado para controlar el modo edición
    var isEditing by remember { mutableStateOf(false) }
    // Estado temporal para los campos editables
    var tempUserName by remember { mutableStateOf("") }
    var tempUserImage by remember { mutableStateOf("") }
    var tempBannerImage by remember { mutableStateOf("") }
    var tempCamperHistory by remember { mutableStateOf("") }
    var tempTagList by remember { mutableStateOf(emptyList<String>()) }
    var newTag by remember { mutableStateOf("") }
    val tagFocusRequester = remember { FocusRequester() }

    LaunchedEffect(userProfileId) {
        try {
            val snapshot = db.collection("users").document(userProfileId).get().await()
            val data = snapshot.data

            data?.let {
                userName = it["user_name"] as? String ?: ""
                userImage = it["user_image"] as? String ?: ""
                bannerImage = it["banner_image"] as? String ?: ""
                email = it["email"] as? String ?: ""
                camperHistory = it["camper_history"] as? String ?: ""
                tagList = it["tag_list"] as? List<String> ?: emptyList()
                visitedPlaces = (it["visited_places"] as? Long)?.toInt() ?: 0
                reviews = (it["reviews"] as? Long)?.toInt() ?: 0

                // Inicializar los valores temporales
                tempUserName = userName
                tempUserImage = userImage
                tempBannerImage = bannerImage
                tempCamperHistory = camperHistory
                tempTagList = tagList
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Error al cargar el perfil: ${e.message}")
            }
        }
    }

    // Función para guardar los cambios
    fun saveChanges() {
        scope.launch {
            try {
                val updates = mapOf(
                    "user_name" to tempUserName,
                    "user_image" to tempUserImage,
                    "banner_image" to tempBannerImage,
                    "camper_history" to tempCamperHistory,
                    "tag_list" to tempTagList
                )

                db.collection("users").document(userProfileId).update(updates).await()

                // Actualizar los valores principales
                userName = tempUserName
                userImage = tempUserImage
                bannerImage = tempBannerImage
                camperHistory = tempCamperHistory
                tagList = tempTagList

                isEditing = false
                snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al actualizar el perfil: ${e.message}")
            }
        }
    }

    // Sección de Logros
    val achievements = remember(visitedPlaces, reviews) {
        listOf(
            // Logros basados en lugares visitados
            AchievementData(
                icon = Icons.Default.Place,
                title = "Primeros pasos",
                description = "Visita tu primer lugar",
                current = minOf(visitedPlaces, 1),
                target = 1,
                color = Color(0xFF0A940F)
            ),
            AchievementData(
                icon = Icons.Default.Explore,
                title = "Explorador novato",
                description = "Visita 5 lugares",
                current = minOf(visitedPlaces, 5),
                target = 5,
                color = Color(0xFF8A61D5)
            ),
            AchievementData(
                icon = Icons.Default.TravelExplore,
                title = "Viajero experimentado",
                description = "Visita 15 lugares",
                current = minOf(visitedPlaces, 15),
                target = 15,
                color = Color(0xE6D770D7)
            ),
            AchievementData(
                icon = Icons.Default.Flag,
                title = "Maestro explorador",
                description = "Visita 30 lugares",
                current = minOf(visitedPlaces, 30),
                target = 30,
                color = Color(0xFF388E3C)
            ),
            AchievementData(
                icon = Icons.Default.Public,
                title = "Leyenda campista",
                description = "Visita 50 lugares",
                current = minOf(visitedPlaces, 50),
                target = 50,
                color = Color(0xFFFFA000)
            ),

            // Logros basados en reseñas
            AchievementData(
                icon = Icons.Default.StarOutline,
                title = "Primera reseña",
                description = "Escribe tu primera reseña",
                current = minOf(reviews, 1),
                target = 1,
                color = Color(0xFF00BCD4)
            ),
            AchievementData(
                icon = Icons.Default.StarHalf,
                title = "Crítico principiante",
                description = "Escribe 5 reseñas",
                current = minOf(reviews, 5),
                target = 5,
                color = Color(0xFF673AB7)
            ),
            AchievementData(
                icon = Icons.Default.Star,
                title = "Experto en reseñas",
                description = "Escribe 20 reseñas",
                current = minOf(reviews, 20),
                target = 20,
                color = Color(0xFFFFC107)
            ),
            AchievementData(
                icon = Icons.Default.AutoAwesome,
                title = "Gurú de reseñas",
                description = "Escribe 50 reseñas",
                current = minOf(reviews, 50),
                target = 50,
                color = Color(0xFFE91E63)
            ),

            // Logros combinados
            AchievementData(
                icon = Icons.Default.ThumbsUpDown,
                title = "Equilibrio perfecto",
                description = "10 lugares + 10 reseñas",
                current = minOf(min(visitedPlaces, reviews), 10),
                target = 10,
                color = Color(0xFF9C27B0)
            ),
            AchievementData(
                icon = Icons.Default.LocalActivity,
                title = "Embajador campista",
                description = "25 lugares + 25 reseñas",
                current = minOf(min(visitedPlaces, reviews), 25),
                target = 25,
                color = Color(0xFF3F51B5)
            ),
            AchievementData(
                icon = Icons.Default.Stars,
                title = "Leyenda total",
                description = "50 lugares + 50 reseñas",
                current = minOf(min(visitedPlaces, reviews), 50),
                target = 50,
                color = Color(0xFFFF5722)
            )
        ).filter { it.target > 0 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sección superior (banner, info usuario, stats)
            item {
                // Banner Image
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
                        modifier = Modifier.fillMaxSize()
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
                            onClick = { /* Handle back navigation */ },
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

                        if (isEditable) {
                            if (isEditing) {
                                Row {
                                    IconButton(
                                        onClick = {
                                            isEditing = false
                                            // Restaurar valores originales
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

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(110.dp)
                            .offset(y = 55.dp)
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

                        // Foto de perfil
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
                                    // Aquí podrías añadir lógica para cambiar la imagen
                                }
                        )
                    }
                }

                // Espacio para compensar la superposición
                Spacer(modifier = Modifier.height(50.dp))

                // Sección de nombre y email
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

                    // Tags
                    if (tagList.isNotEmpty() || isEditing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(if (isEditing) tempTagList else tagList) { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable(enabled = isEditing) {
                                            if (isEditing) {
                                                tempTagList = tempTagList - tag
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                        if (isEditing) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar tag",
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (isEditing) {
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        BasicTextField(
                                            value = newTag,
                                            onValueChange = { newTag = it },
                                            textStyle = MaterialTheme.typography.labelMedium.copy(
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier
                                                .width(80.dp)
                                                .focusRequester(tagFocusRequester),
                                            singleLine = true
                                        )
                                        IconButton(
                                            onClick = {
                                                if (newTag.isNotBlank()) {
                                                    tempTagList = tempTagList + newTag
                                                    newTag = ""
                                                }
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Añadir tag",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sección Camper Story
                    if (camperHistory.isNotEmpty() || isEditing) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Sobre mi",
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

                // Estadísticas
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    // Cuadrado para Lugares visitados
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
                                contentDescription = "Lugares visitados",
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
                                text = "Lugares",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }

                    // Cuadrado para Reseñas
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
                                contentDescription = "Reseñas",
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
                                text = "Reseñas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                }
            }

            // Título de logros
            item {
                Text(
                    text = "Mis Logros (${achievements.count { it.current >= it.target }}/${achievements.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                )
            }

            // Items de logros
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

        // Snackbar para mostrar mensajes
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

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
                        contentDescription = "Completado",
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
                text = if (isCompleted) "¡Completado!"
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