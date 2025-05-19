package com.tfg.campandgo.ui.screen

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.data.model.CamperSite
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.navigation.NavController
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

/**
 * Pantalla de creación de los sitios camper.
 *
 * @param latitude Latitud.
 * @param longitude Longitud.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddCamperSiteScreen(
    latitude: Double,
    longitude: Double,
    navigator: NavController
) {
    // Estado del formulario
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0.0) }
    var amenities by remember { mutableStateOf(listOf<String>()) }
    var newAmenity by remember { mutableStateOf("") }

    // Estado para las imágenes
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showCamera by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedVideoUri by remember { mutableStateOf<Uri?>(null) }


    // Contexto
    val context = LocalContext.current

    // Función para validar campos
    fun validateFields(): Boolean {
        return when {
            name.isBlank() -> {
                Toast.makeText(context,"Por favor, ingresa el nombre del sitio", Toast.LENGTH_SHORT).show()
                false
            }
            address.isBlank() -> {
                Toast.makeText(context,"Por favor, ingresa la dirección del sitio", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    // Lanzador para galería (imágenes y videos)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris?.let { uriList ->
            val newImages = uriList.filter { uri ->
                context.contentResolver.getType(uri)?.startsWith("image/") == true
            }
            val newVideos = uriList.filter { uri ->
                context.contentResolver.getType(uri)?.startsWith("video/") == true
            }
            images = images + newImages
            videos = videos + newVideos
        }
    }

    // Función para crear URIs de video
    fun createVideoUri(): Uri {
        val contentResolver = context.contentResolver
        val videoName = "camper_site_${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, videoName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        }
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    // Lanzador para cámara de video
    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && capturedVideoUri != null) {
            capturedVideoUri?.let { uri ->
                videos = videos + uri
            }
        }
    }

    // Preparar la URI para la imagen a capturar
    fun createImageUri(): Uri {
        val contentResolver = context.contentResolver
        val imageName = "camper_site_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    // Manejar la captura de imagen
    if (showCamera) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            showCamera = false
            if (success && capturedImageUri != null) {
                capturedImageUri?.let { uri ->
                    images = images + uri
                }
            }
        }

        LaunchedEffect(Unit) {
            capturedImageUri = createImageUri()
            launcher.launch(capturedImageUri!!)
        }
    }

    // Diálogo para añadir amenidades
    var showAmenityDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val amenityOptions = listOf("Pernocta", "Wifi", "Agua potable", "Electricidad",
        "Duchas", "Lavandería", "Baños", "Zona de picnic",
        "Barbacoa", "Piscina", "Área infantil", "Aparcamiento",
        "Tienda", "Restaurante", "Recepción 24h", "Alquiler de bicicletas",
        "Zona para mascotas")
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Nuevo Sitio Camper",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            try {
                                navigator.popBackStack()
                            } catch (e: Exception) {
                                navigator.navigateUp()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (validateFields()) {
                        try {
                            scope.launch {
                                try {
                                    val (imageUrls, videoUrls) = uploadMediaToFirebaseStorage(context, images, videos)

                                    val newCamperSite = CamperSite(
                                        id = UUID.randomUUID().toString(),
                                        name = name,
                                        formattedAddress = address,
                                        description = description,
                                        mainImageUrl = imageUrls.firstOrNull() ?: "",
                                        images = imageUrls,
                                        videos = videoUrls,
                                        rating = rating,
                                        reviewCount = 1,
                                        amenities = amenities,
                                        reviews = emptyList(),
                                        location = GeoPoint(latitude, longitude)
                                    )

                                    saveCamperSiteToFirestore(newCamperSite)
                                    withContext(Dispatchers.Main) {
                                        navigator.popBackStack()
                                    }
                                } catch (e: Exception) {
                                    Log.e("AddCamperSite", "Error saving camper site", e)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Guardar",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Imagenes del sitio",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Images grid items
                    items(images) { uri ->
                        MediaThumbnail(
                            uri = uri,
                            isVideo = false,
                            onDelete = { images = images - uri }
                        )
                    }

                    // Videos grid items
                    items(videos) { uri ->
                        MediaThumbnail(
                            uri = uri,
                            isVideo = true,
                            onDelete = { videos = videos - uri }
                        )
                    }

                    // Add button to pick media from gallery
                    item {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable {
                                    galleryLauncher.launch("image/* video/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = "Añadir desde galería",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Button to record video
                    item {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable {
                                    capturedVideoUri = createVideoUri()
                                    videoCaptureLauncher.launch(capturedVideoUri!!)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.Videocam),
                                contentDescription = "Grabar video",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Button to open camera for taking photo
                    item {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable {
                                    showCamera = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Añadir imagen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Información básica
            Text(
                text = "Información básica",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del sitio*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rating
            Text(
                text = "Valoración",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Puntuación: ", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "%.1f".format(rating),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Slider(
                        value = rating.toFloat(),
                        onValueChange = { newValue ->
                            rating = "%.2f".format(Locale.US, newValue).toDouble()
                        },
                        valueRange = 0f..5f,
                        steps = 40,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Servicios
            Text(
                text = "Servicio",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (amenities.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            items(amenities) { amenity ->
                                AssistChip(
                                    onClick = { amenities = amenities - amenity },
                                    label = { Text(amenity) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showAmenityDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Añadir servicio")
                    }
                }
            }
        }
    }


    if (showAmenityDialog) {
        AlertDialog(
            onDismissRequest = { showAmenityDialog = false },
            title = { Text("Seleccionar servicios", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    // Menú desplegable
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (newAmenity.isEmpty()) "Selecciona un servicio" else newAmenity,
                            color = if (newAmenity.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            amenityOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        newAmenity = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Mostrar servicios seleccionados
                    if (amenities.isNotEmpty()) {
                        Text(
                            text = "Servicios seleccionados:",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        FlowRow(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            amenities.forEach { amenity ->
                                AssistChip(
                                    onClick = { amenities = amenities - amenity },
                                    label = { Text(amenity) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAmenity.isNotBlank() && !amenities.contains(newAmenity)) {
                            amenities = amenities + newAmenity
                            newAmenity = ""
                        }
                    },
                    enabled = newAmenity.isNotBlank() && !amenities.contains(newAmenity),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAmenityDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

/**
 * Guarda el sitio camper en Firestore.
 *
 * @param camperSite El CamperSite que es almacenado.
 */
@Composable
fun MediaThumbnail(uri: Uri, isVideo: Boolean, onDelete: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }

    if (isVideo) {
        LaunchedEffect(uri) {
            withContext(Dispatchers.IO) {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                try {
                    mediaMetadataRetriever.setDataSource(context, uri)
                    val frame = mediaMetadataRetriever.getFrameAtTime(1000000)
                    bitmap.value = frame
                } catch (e: Exception) {
                    Log.e("VideoThumbnail", "Error al obtener miniatura", e)
                } finally {
                    mediaMetadataRetriever.release()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
    ) {
        if (isVideo) {
            if (bitmap.value != null) {
                Image(
                    bitmap = bitmap.value!!.asImageBitmap(),
                    contentDescription = "Miniatura de video",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.Videocam),
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Media del sitio",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        // Botón para eliminar
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Eliminar",
                tint = Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(4.dp)
            )
        }

        // Indicador de video
        if (isVideo) {
            Icon(
                painter = rememberVectorPainter(Icons.Default.Videocam),
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(20.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

/**
 * Guarda el sitio camper en Firestore.
 *
 * @param camperSite El CamperSite que es almacenado.
 */
fun saveCamperSiteToFirestore(camperSite: CamperSite) {
    val db = Firebase.firestore

    try {
        val camperSiteData = hashMapOf(
            "id" to camperSite.id,
            "name" to camperSite.name,
            "formatted_address" to camperSite.formattedAddress,
            "description" to camperSite.description,
            "main_image_url" to camperSite.mainImageUrl,
            "images" to camperSite.images,
            "videos" to camperSite.videos,
            "rating" to camperSite.rating,
            "review_count" to camperSite.reviewCount,
            "amenities" to camperSite.amenities,
            "reviews" to listOf<DocumentReference>(),
            "location" to GeoPoint(camperSite.location.latitude, camperSite.location.longitude)
        )

        db.collection("camper_sites")
            .document(camperSite.id)
            .set(camperSiteData)
            .addOnSuccessListener {
                Log.d("Firestore", "Documento añadido con ID: ${camperSite.id}")
            }
            .addOnFailureListener { e ->
                Log.d("Firestore", "Error añadiendo documento", e)
            }
    } catch (e: Exception) {
        Log.d("Firestore", "Error preparando datos", e)
    }
}

suspend fun uploadMediaToFirebaseStorage(
    context: Context,
    images: List<Uri>,
    videos: List<Uri>
): Pair<List<String>, List<String>> = withContext(Dispatchers.IO) {
    val storage = FirebaseStorage.getInstance().reference
    val imageUrls = mutableListOf<String>()
    val videoUrls = mutableListOf<String>()

    // Subir imágenes
    for (uri in images) {
        try {
            val fileName = "camper_sites/images/${UUID.randomUUID()}.jpg"
            val fileRef = storage.child(fileName)
            val uploadTask = fileRef.putFile(uri).await()
            val url = fileRef.downloadUrl.await().toString()
            imageUrls.add(url)
        } catch (e: Exception) {
            Log.e("Storage", "Error al subir imagen: ${uri}", e)
        }
    }

    // Subir videos
    for (uri in videos) {
        try {
            val fileName = "camper_sites/videos/${UUID.randomUUID()}.mp4"
            val fileRef = storage.child(fileName)
            val uploadTask = fileRef.putFile(uri).await()
            val url = fileRef.downloadUrl.await().toString()
            videoUrls.add(url)
        } catch (e: Exception) {
            Log.e("Storage", "Error al subir video: ${uri}", e)
        }
    }

    return@withContext Pair(imageUrls, videoUrls)
}