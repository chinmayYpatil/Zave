package com.example.zave.ui.results

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder // ADDED IMPORTS
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.zave.domain.models.Place
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.zave.ui.theme.DarkBackground
import com.example.zave.ui.theme.TextPrimary
import com.example.zave.ui.theme.TextSecondary
import com.example.zave.ui.theme.CardBackground
import com.example.zave.ui.theme.AccentPink
import com.example.zave.ui.theme.AccentBlue // ADDED IMPORT
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    navController: NavController,
    query: String, // Received from navigation arguments
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    // ADDED: Collect the set of saved place IDs from the ViewModel
    val savedPlaceIds by viewModel.savedPlaceIds.collectAsState()

    // State to toggle between List (true) and Map (false) view
    var isListView by remember { mutableStateOf(true) }

    Scaffold(
        // Set the main screen background color
        containerColor = DarkBackground,
        topBar = {
            ResultsAppBar(navController = navController, query = query, isListView = isListView) {
                isListView = !isListView
                viewModel.selectPlace(null) // Clear selection when switching views
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            when (uiState) {
                ResultsUiState.Loading -> LoadingState()
                ResultsUiState.Empty -> EmptyState(query)
                is ResultsUiState.Error -> ErrorState((uiState as ResultsUiState.Error).message)
                is ResultsUiState.Success -> {
                    val places = (uiState as ResultsUiState.Success).places

                    if (isListView) {
                        PlaceListView(
                            places = places,
                            savedPlaceIds = savedPlaceIds, // PASS SAVED IDs
                            onPlaceSelected = viewModel::selectPlace,
                            onToggleFavorite = viewModel::toggleSavedStatus // PASS TOGGLE ACTION
                        )
                    } else {
                        MapViewComposable(places = places, selectedPlace = selectedPlace)
                    }
                }
            }
        }
    }
}

// --- Helper Composables ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsAppBar(navController: NavController, query: String, isListView: Boolean, onToggleView: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                // Set text color for visibility on dark background
                Text(query, style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
                Text("Nearby Stores", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary.copy(alpha = 0.7f)))

            }
        },
        actions = {
            IconButton(onClick = onToggleView) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    Icon(
                        if (isListView) Icons.Default.Map else Icons.Default.List,
                        contentDescription = if (isListView) "Show Map" else "Show List",
                        tint = TextPrimary
                    )
                    Text(
                        text = if (isListView) "Map" else "List",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }
        },
        // Set the TopAppBar background color
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBackground,
            navigationIconContentColor = TextPrimary,
            actionIconContentColor = TextPrimary
        )
    )
}

@Composable
fun PlaceListView(
    places: List<Place>,
    savedPlaceIds: Set<String>, // ADDED PARAMETER
    onPlaceSelected: (Place) -> Unit,
    onToggleFavorite: (Place) -> Unit // ADDED PARAMETER
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(places) { place ->
            val isSaved = savedPlaceIds.contains(place.id) // CHECK SAVED STATUS
            StoreCard(
                place = place,
                isSaved = isSaved, // PASS STATUS
                onClick = { onPlaceSelected(place) },
                onToggleFavorite = { onToggleFavorite(place) } // PASS TOGGLE ACTION
            )
        }
    }
}

@Composable
fun StoreCard(
    place: Place,
    isSaved: Boolean, // ADDED PARAMETER
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit // ADDED PARAMETER
) {
    val context = LocalContext.current

    // Determine open status display (MODIFIED)
    val openStatusText = when (place.openNow) {
        true -> "Open Now"
        false -> "Closed"
        null -> "Status Unknown"
    }

    // Determine status color (MODIFIED)
    val statusColor = when (place.openNow) {
        true -> Color(0xFF4CAF50) // Green for Open
        false -> AccentPink // AccentPink for Closed (using a defined theme color)
        null -> TextSecondary // Secondary text color for Unknown
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Use CardBackground for the card container
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Icon and Place Info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = place.iconUrl,
                    contentDescription = "${place.name} icon",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Set text color for visibility
                    Text(place.name, style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
                    Text(
                        "${place.vicinity}",
                        style = MaterialTheme.typography.bodyMedium,
                        // Use TextSecondary for less prominent text
                        color = TextSecondary
                    )
                }
            }


            // Right Column: Status, Rating, Directions, and Favorite Button
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Favorite Toggle Button (ADDED)
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isSaved) "Unsave" else "Save",
                            tint = if (isSaved) AccentPink else TextSecondary.copy(alpha = 0.7f) // Use AccentPink for saved
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        // Display Open/Closed status
                        Text(
                            openStatusText,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold // Make status bold
                            )
                        )
                        // Kept rating display
                        Text(
                            "Rating: ${place.rating ?: "N/A"}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }

                // Button to open in Google Maps directly
                Button(
                    onClick = { openGoogleMaps(context, place.lat, place.lng, place.name) },
                    modifier = Modifier.padding(top = 4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue) // Use AccentBlue for primary action
                ) {
                    Text("Directions", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// --- Map View Implementation ---

@Composable
fun MapViewComposable(places: List<Place>, selectedPlace: Place?) {
    val initialLocation = LatLng(places.first().lat, places.first().lng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        places.forEach { place ->
            val position = LatLng(place.lat, place.lng)
            Marker(
                state = MarkerState(position = position),
                title = place.name,
                snippet = place.vicinity
            )
        }
    }

    // Simple way to handle showing details for selected place (optional UI)
    if (selectedPlace != null) {
        // You would typically show a bottom sheet or a detail card here
    }
}

// --- Error/Status Composables ---

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Use AccentBlue for the indicator color against dark background
        CircularProgressIndicator(color = AccentBlue)
    }
}

@Composable
fun EmptyState(query: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        // Set text color for visibility
        Text("No results found for '$query' within your search radius.", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary))
    }
}

@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        // Use AccentPink for error text in the dark theme
        Text("Error: $message", color = AccentPink, style = MaterialTheme.typography.titleMedium)
    }
}

// --- Intent Helper ---

fun openGoogleMaps(context: Context, lat: Double, lng: Double, name: String) {
    val uri = Uri.parse("geo:$lat,$lng?q=$name")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Google Maps app not found.", Toast.LENGTH_SHORT).show()
    }
}
