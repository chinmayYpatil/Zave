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
import androidx.compose.material.icons.filled.* // FIX: Wildcard import added
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

@OptIn(ExperimentalMaterial3Api::class) // FIX: OptIn for Scaffold
@Composable
fun ResultsScreen(
    navController: NavController,
    query: String, // Received from navigation arguments
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()

    // State to toggle between List (true) and Map (false) view
    var isListView by remember { mutableStateOf(true) }

    Scaffold(
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
                        PlaceListView(places, viewModel::selectPlace)
                    } else {
                        MapViewComposable(places = places, selectedPlace = selectedPlace)
                    }
                }
            }
        }
    }
}

// --- Helper Composables ---

@OptIn(ExperimentalMaterial3Api::class) // FIX: OptIn for TopAppBar
@Composable
fun ResultsAppBar(navController: NavController, query: String, isListView: Boolean, onToggleView: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(query, style = MaterialTheme.typography.titleMedium)
                Text("Nearby Stores", style = MaterialTheme.typography.labelSmall)
            }
        },
        actions = {
            IconButton(onClick = onToggleView) {
                Icon(
                    if (isListView) Icons.Default.Map else Icons.Default.List,
                    contentDescription = if (isListView) "Show Map" else "Show List"
                )
            }
        }
    )
}

@Composable
fun PlaceListView(places: List<Place>, onPlaceSelected: (Place) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(places) { place ->
            StoreCard(place = place, onClick = { onPlaceSelected(place) })
        }
    }
}

@Composable
fun StoreCard(place: Place, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = place.iconUrl,
                contentDescription = "${place.name} icon",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${place.vicinity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val distanceKm = place.distanceMeters?.let { it / 1000.0 }
                if (distanceKm != null) {
                    Text(
                        "${String.format("%.1f", distanceKm)} km",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Text(
                    "Rating: ${place.rating ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall
                )

                // Button to open in Google Maps directly
                Button(
                    onClick = { openGoogleMaps(context, place.lat, place.lng, place.name) },
                    modifier = Modifier.padding(top = 4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
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
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(query: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("No results found for '$query' within your search radius.", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("Error: $message", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
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