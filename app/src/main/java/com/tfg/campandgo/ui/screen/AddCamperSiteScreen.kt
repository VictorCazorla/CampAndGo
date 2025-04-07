package com.tfg.campandgo.ui.screen

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.wear.compose.material.Chip
import coil.compose.rememberImagePainter

import com.tfg.campandgo.data.model.CamperSite
import com.tfg.campandgo.data.model.CamperSiteReview
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddCamperSiteScreen(
    onSave: (CamperSite) -> Unit,
    onCancel: () -> Unit
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
    var showCamera by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }


    // Contexto
    val context = LocalContext.current

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
            //launcher.launch(capturedImageUri)
        }
    }

    // Diálogo para añadir amenidades
    var showAmenityDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Añadir Nuevo Sitio Camper",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo para el nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del sitio") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo para la dirección
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo para la descripción
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo para el rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rating: ")
            Slider(
                value = rating.toFloat(),
                onValueChange = { rating = it.toDouble() },
                valueRange = 0f..5f,
                steps = 4,
                modifier = Modifier.weight(1f)
            )
            Text(text = "%.1f".format(rating))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Amenidades
        Text("Amenidades:", style = MaterialTheme.typography.titleMedium)
        LazyRow {
            items(amenities) { amenity ->
                Button(
                    onClick = { amenities = amenities - amenity },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(amenity)
                }
            }
        }

        Button(
            onClick = { showAmenityDialog = true },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Añadir Amenidad")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de imágenes
        Text("Imágenes:", style = MaterialTheme.typography.titleMedium)

        // Grid de imágenes
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(200.dp)
        ) {
            items(images) { uri ->
                Image(
                    painter = rememberImagePainter(uri),
                    contentDescription = "Imagen del sitio",
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .clickable {
                            // Opcional: Ver imagen en grande
                        }
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .border(1.dp, Color.Gray)
                        .clickable {
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir imagen")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Cancelar", color = Color.Black)
            }

            Button(
                onClick = {
                    val newCamperSite = CamperSite(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        formattedAddress = address,
                        description = description,
                        mainImageUrl = images.firstOrNull()?.toString() ?: "",
                        images = images.map { it.toString() },
                        rating = rating,
                        reviewCount = 0,
                        amenities = amenities,
                        reviews = emptyList()
                    )
                    onSave(newCamperSite)
                },
                enabled = name.isNotBlank() && address.isNotBlank()
            ) {
                Text("Guardar")
            }
        }
    }

    // Diálogo para añadir amenidad
    if (showAmenityDialog) {
        AlertDialog(
            onDismissRequest = { showAmenityDialog = false },
            title = { Text("Añadir Amenidad") },
            text = {
                OutlinedTextField(
                    value = newAmenity,
                    onValueChange = { newAmenity = it },
                    label = { Text("Nombre de la amenidad") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAmenity.isNotBlank()) {
                            amenities = amenities + newAmenity
                            newAmenity = ""
                            showAmenityDialog = false
                        }
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAmenityDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}