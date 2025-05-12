package com.tfg.campandgo.ui.component

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

/**
 * Una función composable que se encarga de obtener la ubicación actual del usuario.
 * Utiliza el cliente de ubicación de Google (Fused Location Provider) para acceder
 * a las coordenadas GPS si se ha otorgado el permiso correspondiente.
 *
 * @param onLocationFetched Callback que se ejecuta al obtener la ubicación. Recibe un objeto `LatLng`
 *                          con la latitud y longitud de la ubicación actual del usuario.
 */
@Composable
fun LocationFetcher(onLocationFetched: (LatLng) -> Unit) {
    // Obtiene el contexto actual
    val context = LocalContext.current
    // Instancia del cliente de ubicación de Google
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Lanza un efecto al inicializar el componente
    LaunchedEffect(Unit) {
        // Comprueba si se tiene el permiso de ubicación precisa
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { onLocationFetched(LatLng(it.latitude, it.longitude)) }
            }
        }
    }
}
