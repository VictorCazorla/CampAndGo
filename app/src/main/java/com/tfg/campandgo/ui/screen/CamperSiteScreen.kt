package com.tfg.campandgo.ui.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tfg.campandgo.R
import com.tfg.campandgo.data.api.WeatherRetrofitClient
import com.tfg.campandgo.data.model.CamperSite
import com.tfg.campandgo.data.model.CamperSiteReview
import kotlinx.coroutines.tasks.await
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CamperSiteScreen(
    camperSiteID: String,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // OpenWeather
    var nameWeather by remember { mutableStateOf<String?>("") }
    var tempWeather by remember { mutableStateOf<Double?>(0.0) }
    var humidityWeather by remember { mutableStateOf<Int?>(0) }
    var descriptionWeather by remember { mutableStateOf<String?>("") }
    var iconWeather by remember { mutableStateOf<String?>("") }
    val context = LocalContext.current
    val openWeatherKey = context.getString(R.string.open_weather_key)
    val openWeatherUnit = context.getString(R.string.open_weather_unit)
    val openWeatherLang = context.getString(R.string.open_weather_lang)

    var site by remember { mutableStateOf(
        CamperSite(
            "", "", "", "", "", listOf(""), 0.0, 0, listOf(""), listOf(CamperSiteReview("", 0.0, "", "", listOf())), GeoPoint(0.0,0.0)
        )
    ) }
    val db = Firebase.firestore

    LaunchedEffect(Unit) {
        try {
            val camperSitesSnapshot = db.collection("camper_sites")
                .whereEqualTo("id", camperSiteID)
                .get()
                .await()  // Espera respuesta de manera síncrona

            if (camperSitesSnapshot.isEmpty) {
                Log.d("LaunchCampsite", "No camper site found")
            } else {
                val camperSite = camperSitesSnapshot.documents.first()
                site = CamperSite(
                    id = camperSite.getString("id") ?: "",
                    name = camperSite.getString("name") ?: "",
                    formattedAddress = camperSite.getString("formatted_address") ?: "",
                    description = camperSite.getString("description") ?: "",
                    mainImageUrl = camperSite.getString("main_image_url") ?: "",
                    images = camperSite.get("images") as? List<String> ?: listOf(),
                    rating = camperSite.getDouble("rating") ?: 0.0,
                    reviewCount = camperSite.getLong("review_count")?.toInt() ?: 0,
                    amenities = camperSite.get("amenities") as? List<String> ?: listOf(),
                    reviews = (camperSite.get("reviews") as? List<DocumentReference>)?.map {
                        val reviewDoc = it.get().await()

                        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                        CamperSiteReview(
                            userName = reviewDoc.getString("user_name") ?: "",
                            rating = reviewDoc.getDouble("rating") ?: 0.0,
                            comment = reviewDoc.getString("comment") ?: "",
                            date = reviewDoc.getDate("date")?.toInstant()?.atZone(ZoneId.systemDefault())
                                ?.toLocalDate()?.format(formatter)
                                ?: "",
                            images = reviewDoc.get("images") as? List<String> ?: listOf()
                        )
                    } ?: listOf(),
                    location = camperSite.getGeoPoint("location")!!
                )
            }

            val response = WeatherRetrofitClient.weatherService.getCurrentWeather(
                lat = site.location.latitude,
                lon = site.location.longitude,
                apiKey = openWeatherKey,
                units = openWeatherUnit,
                lang = openWeatherLang
            )
            nameWeather = response.name
            tempWeather = response.main.temp
            humidityWeather = response.main.humidity
            descriptionWeather = response.weather[0].description
            iconWeather = response.weather[0].icon

        } catch (e: Exception) {
            Log.d("LaunchCampsite", "Error fetching camper site", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFFF5F5F5))
    ) {
        // Header with image and back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Main header image
            Image(
                painter = rememberAsyncImagePainter(site.mainImageUrl),
                contentDescription = "Campsite header image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay at bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0f,
                            endY = 100f
                        )
                    )
            )

            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // Title and address
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Text(
                    text = site.formattedAddress,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Main content
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Rating and review count box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Rating box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .background(Color(0xFFFFEB3B), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "Rating",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(site.rating),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Review count box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "Review Count",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Text(
                            text = "${site.reviewCount} reviews",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Weather box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Weather",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (tempWeather != null && descriptionWeather != null && humidityWeather != null && nameWeather != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Temperature
                            Text(
                                text = "${"%.1f".format(tempWeather)}°C",
                                style = MaterialTheme.typography.displaySmall,
                                color = if (tempWeather!! > 20) Color(0xFFB71C1C) else Color(0xFF2196F3)
                            )

                            // Location and condition data
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = nameWeather!!,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = descriptionWeather!!.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Humidity: $humidityWeather%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Loading weather data...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Description section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = site.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Amenities section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Amenities",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        site.amenities.forEach { amenity ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE3F2FD), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = amenity,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                    }
                }
            }

            // Photo gallery section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column {
                    Text(
                        text = "Photos",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(site.images) { imageUrl ->
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = "Site photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Reviews section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    site.reviews.take(3).forEach { review ->
                        ReviewItem(review = review)
                        if (review != site.reviews.last()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    if (site.reviews.size > 3) {
                        TextButton(
                            onClick = { /* Show all reviews */ },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("See all ${site.reviews.size} reviews")
                        }
                    }
                }
            }
        }
    }

    // Floating action button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = onBookClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF5722),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Book Now",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ReviewItem(review: CamperSiteReview) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.LightGray, CircleShape)
        ) {
            // User avatar placeholder
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = review.userName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "%.1f".format(review.rating),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (!review.images.isNullOrEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(review.images) { imageUrl ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = "Review photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
