import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tfg.campandgo.ui.viewmodel.MapsViewModel
import com.tfg.campandgo.ui.components.*
import com.tfg.campandgo.ui.screen.ErrorScreen
import com.tfg.campandgo.ui.screen.MapScreen

@Composable
fun HomeScreen() {
    val viewModel: MapsViewModel = viewModel()
    val context = LocalContext.current
    val apiKey = remember { getApiKeyFromManifest(context) }
    var searchQuery by remember { mutableStateOf("") }

    if (apiKey == null) {
        ErrorScreen(message = "Error: API Key no configurada en el Manifest")
        return
    }

    PermissionHandler(viewModel)

    if (viewModel.hasLocationPermission.value) {
        LocationFetcher { location ->
            viewModel.selectedLocation.value = location
            viewModel.fetchNearbyPlaces(viewModel.selectedLocation.value!!, apiKey)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            viewModel.searchLocations(searchQuery, apiKey)
        }
    }

    MapScreen(
        currentLocation = viewModel.selectedLocation.value,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onSearch = { placeId -> viewModel.getLocationDetails(placeId, apiKey) },
        searchSuggestions = viewModel.searchSuggestions,
        errorMessage = viewModel.errorMessage.value,
        nearbyPlaces = viewModel.nearbyPlaces,
        viewModel = viewModel
    )
}
private fun getApiKeyFromManifest(context: Context): String? {
    return try {
        val appInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        appInfo.metaData.getString("com.google.android.geo.API_KEY")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}












