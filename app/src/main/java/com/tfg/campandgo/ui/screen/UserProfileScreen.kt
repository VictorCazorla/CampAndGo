import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tfg.campandgo.R

data class Achievement(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String,
    val currentProgress: Int,
    val target: Int,
    val color: Color,
    val category: AchievementCategory
) {
    val isUnlocked: Boolean get() = currentProgress >= target
    val progress: Float get() = currentProgress.toFloat() / target.toFloat()
}

enum class AchievementCategory {
    TRAVEL, REVIEW
}

@Composable
fun UserProfileScreen(
    profileImageUri: String? = null,
    bannerImageUri: String? = null,
    userName: String = "Aventurero Camper",
    userDescription: String = "Explorando los rincones más bellos",
    visitedSitesCount: Int = 12,
    reviewsCount: Int = 8,
    userStory: String = "Desde que descubrí el mundo camper, no he parado de explorar. Mis lugares favoritos son...",
    onAddTag: (String) -> Unit = {},
) {
    val achievements = generateAchievements(
        visitedSites = visitedSitesCount,
        reviews = reviewsCount,
    )

    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }

    val availableTags = listOf(
        "Viajero" to Icons.Default.DirectionsCar,
        "Fotógrafo" to Icons.Default.CameraAlt,
        "Cocinero" to Icons.Default.Restaurant,
        "Explorador" to Icons.Default.Map,
        "Aventurero" to Icons.Default.Hiking,
        "Naturaleza" to Icons.Default.Park,
        "Acampada" to Icons.Default.Forest,
        "Rutas" to Icons.Default.Directions
    )

    val userTags = remember { mutableStateListOf("Viajero") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Banner section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (bannerImageUri != null) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "Banner del perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp)
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .shadow(8.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        userTags.forEach { tag ->
                            val icon = availableTags.firstOrNull { it.first == tag }?.second
                                ?: Icons.Default.Label
                            UserTag(
                                text = tag,
                                icon = icon,
                                onRemove = { userTags.remove(tag) }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showAddTagDialog = true }
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir etiqueta",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Añadir",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mi historia camper",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = userStory,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    icon = Icons.Default.Place,
                    count = visitedSitesCount,
                    label = "Sitios",
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    icon = Icons.Default.Star,
                    count = reviewsCount,
                    label = "Opiniones",
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }
            val filteredAchievements = achievements.filter {
                selectedCategory == null || it.category == selectedCategory
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logros (${achievements.count { it.isUnlocked }}/${achievements.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Achievement categories filter
                ScrollableTabRow(
                    selectedTabIndex = AchievementCategory.values().indexOfFirst { it == selectedCategory } + 1,
                    edgePadding = 0.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(
                                tabPositions[selectedCategory?.let {
                                    AchievementCategory.values().indexOf(it) + 1
                                } ?: 0]
                            ),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Tab(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        text = { Text("Todos") }
                    )

                    AchievementCategory.values().forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = {
                                Text(
                                    text = when(category) {
                                        AchievementCategory.TRAVEL -> "Viajes"
                                        AchievementCategory.REVIEW -> "Opiniones"
                                    }
                                )
                            }
                        )
                    }
                }

                if (filteredAchievements.isEmpty()) {
                    Text(
                        text = "No hay logros en esta categoría",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredAchievements) { achievement ->
                            Box(modifier = Modifier.clickable { selectedAchievement = achievement }) {
                                AchievementItem(achievement = achievement)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Achievement details dialog
    selectedAchievement?.let { achievement ->
        AlertDialog(
            onDismissRequest = { selectedAchievement = null },
            confirmButton = {
                TextButton(onClick = { selectedAchievement = null }) {
                    Text("Cerrar")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = achievement.icon,
                        contentDescription = null,
                        tint = achievement.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(achievement.name)
                }
            },
            text = {
                Column {
                    Text(achievement.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!achievement.isUnlocked) {
                        Text(
                            text = "Progreso: ${achievement.currentProgress}/${achievement.target}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        LinearProgressIndicator(
                            progress = achievement.progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = achievement.color,
                            trackColor = achievement.color.copy(alpha = 0.2f)
                        )
                    } else {
                        Text(
                            text = "¡Logro desbloqueado!",
                            style = MaterialTheme.typography.bodySmall,
                            color = achievement.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }

    // Add tag dialog
    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("Añadir etiqueta") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTagText,
                        onValueChange = { newTagText = it },
                        label = { Text("Nombre de la etiqueta") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Etiquetas sugeridas:", style = MaterialTheme.typography.labelMedium)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        availableTags.forEach { (tag, icon) ->
                            if (tag !in userTags) {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        newTagText = tag
                                    },
                                    label = { Text(tag) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagText.isNotBlank() && newTagText !in userTags) {
                            onAddTag(newTagText)
                            userTags.add(newTagText)
                            newTagText = ""
                        }
                        showAddTagDialog = false
                    },
                    enabled = newTagText.isNotBlank() && newTagText !in userTags
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserTag(
    text: String,
    icon: ImageVector,
    onRemove: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(50)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = if (onRemove != null) 4.dp else 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        onRemove?.let { remove ->
            IconButton(
                onClick = remove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar etiqueta",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, count: Int, label: String, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // Añade esta línea para centrar verticalmente
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth() // Asegura que ocupe todo el ancho disponible
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center, // Centra el texto horizontalmente
                modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center, // Centra el texto horizontalmente
                modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho
            )
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                achievement.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (achievement.isUnlocked) {
                achievement.color
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = achievement.name,
                    tint = if (achievement.isUnlocked) {
                        achievement.color
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(28.dp)
                )

                if (achievement.isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Logro desbloqueado",
                        tint = achievement.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (!achievement.isUnlocked) {
                    Text(
                        text = "${achievement.currentProgress}/${achievement.target}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                LinearProgressIndicator(
                    progress = achievement.progress.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (achievement.isUnlocked) {
                        achievement.color
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                    trackColor = if (achievement.isUnlocked) {
                        achievement.color.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    }
                )
            }
        }
    }
}

fun generateAchievements(
    visitedSites: Int,
    reviews: Int,
): List<Achievement> {
    return listOf(
        // Travel achievements
        Achievement(
            id = "first_steps",
            name = "Primeros pasos",
            icon = Icons.Default.DirectionsWalk,
            description = "Visita tu primer sitio camper",
            currentProgress = minOf(visitedSites, 1),
            target = 1,
            color = Color(0xFF4CAF50), // Verde
            category = AchievementCategory.TRAVEL
        ),
        Achievement(
            id = "explorer",
            name = "Explorador",
            icon = Icons.Default.Explore,
            description = "Visita 5 sitios camper",
            currentProgress = minOf(visitedSites, 5),
            target = 5,
            color = Color(0xFF2196F3), // Azul
            category = AchievementCategory.TRAVEL
        ),
        Achievement(
            id = "seasoned_traveler",
            name = "Viajero experimentado",
            icon = Icons.Default.DirectionsCar,
            description = "Visita 15 sitios camper",
            currentProgress = minOf(visitedSites, 15),
            target = 15,
            color = Color(0xFF9C27B0), // Morado
            category = AchievementCategory.TRAVEL
        ),
        Achievement(
            id = "nomad",
            name = "Nómada",
            icon = Icons.Default.Public,
            description = "Visita 30 sitios camper",
            currentProgress = minOf(visitedSites, 30),
            target = 30,
            color = Color(0xFFFF5722), // Naranja
            category = AchievementCategory.TRAVEL
        ),

        // Review achievements
        Achievement(
            id = "critic",
            name = "Crítico",
            icon = Icons.Default.RateReview,
            description = "Escribe 5 reseñas",
            currentProgress = minOf(reviews, 5),
            target = 5,
            color = Color(0xFFFFC107), // Amarillo
            category = AchievementCategory.REVIEW
        ),
        Achievement(
            id = "storyteller",
            name = "Narrador",
            icon = Icons.Default.Book,
            description = "Escribe 10 reseñas",
            currentProgress = minOf(reviews, 10),
            target = 10,
            color = Color(0xFF607D8B), // Gris azulado
            category = AchievementCategory.REVIEW
        ),
        Achievement(
            id = "expert_reviewer",
            name = "Experto en reseñas",
            icon = Icons.Default.Star,
            description = "Escribe 25 reseñas",
            currentProgress = minOf(reviews, 25),
            target = 25,
            color = Color(0xFFE91E63), // Rosa
            category = AchievementCategory.REVIEW
        ),

    ).sortedByDescending { it.isUnlocked }
}