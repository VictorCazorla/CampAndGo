package com.tfg.campandgo.ui.component

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.tfg.campandgo.ui.viewmodel.MapsViewModel

/**
 * Una función composable que maneja la solicitud de permisos para acceder a la ubicación del usuario.
 * Solicita el permiso de ubicación precisa (ACCESS_FINE_LOCATION) y muestra un cuadro de diálogo
 * en caso de que el usuario rechace el permiso.
 *
 * @param viewModel El `MapsViewModel` que contiene el estado del permiso de ubicación.
 */
@Composable
fun PermissionHandler(viewModel: MapsViewModel) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Lanza una solicitud de permiso utilizando un ActivityResultLauncher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.hasLocationPermission.value = isGranted
        if (!isGranted) showPermissionDialog = true
    }

    LaunchedEffect(Unit) {
        when {
            // Verifica si el permiso ya ha sido otorgado
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.hasLocationPermission.value = true
            }
            else -> permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) // Solicita el permiso si no ha sido otorgado
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission required") },
            text = { Text("We need access to your location to display the map.") },
            confirmButton = {
                Button(onClick = {
                    // Vuelve a solicitar el permiso cuando el usuario lo confirma
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    showPermissionDialog = false
                }) { Text("Retry") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel") }
            }
        )
    }
}
