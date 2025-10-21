package com.example.zave.ui.home

import android.Manifest
import android.location.Location
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // FIX: Add wildcard import for Icons.Default.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.zave.ui.common.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class) // FIX: OptIn for experimental Material 3 APIs
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val queryInput by viewModel.queryInput.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Initialize Remote Config values on first composition
    LaunchedEffect(Unit) {
        viewModel.setRemoteConfigInitialValues()
    }

    // --- Location Permission Handling ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permission granted, request location
            viewModel.requestLocation()
        } else {
            // Permission denied
            viewModel.setPermissionRequired(true)
        }
    }

    // Effect to trigger permission request when needed
    LaunchedEffect(uiState.requiresLocationPermission) {
        if (!uiState.currentLocation.isReady() && !uiState.requiresLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // --- Search Trigger ---
    val onSearch = {
        val query = viewModel.getEffectiveSearchQuery()
        focusManager.clearFocus()
        if (uiState.currentLocation.isReady()) {
            navController.navigate(Screen.Results.createRoute(query))
        } else {
            // If location is missing, re-request it or show warning
            viewModel.requestLocation()
            Toast.makeText(context, "Location required. Trying to fetch...", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            HomeAppBar(navController = navController)
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 1. Remote Config Banner
            RemoteConfigBanner(message = uiState.bannerMessage)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Search Bar
            OutlinedTextField(
                value = queryInput,
                onValueChange = viewModel::updateQueryInput,
                label = { Text("Search product or brand (e.g., ${uiState.featuredCategory})") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Location Status and Error
            LocationStatusView(uiState = uiState, onPermissionRequest = {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            })

            // 4. Recent Searches
            if (uiState.recentSearches.isNotEmpty()) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.recentSearches.take(3)) { item ->
                        InputChip(
                            selected = false,
                            onClick = {
                                viewModel.updateQueryInput(item.query)
                                onSearch()
                            },
                            label = { Text(item.query) }
                        )
                    }
                }
            }
        }
    }
}

// --- Helper Composable Functions ---

@OptIn(ExperimentalMaterial3Api::class) // FIX: OptIn for TopAppBar
@Composable
fun HomeAppBar(navController: NavController) {
    TopAppBar(
        title = { Text("Shopper's Compass") },
        actions = {
            IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

@Composable
fun RemoteConfigBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun LocationStatusView(uiState: HomeUiState, onPermissionRequest: () -> Unit) {
    if (uiState.isLoading) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Initializing app configuration...")
        }
    } else if (uiState.requiresLocationPermission) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onPermissionRequest),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                "Location permission denied. Tap to grant access.",
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
    } else if (uiState.error != null || !uiState.currentLocation.isReady()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                uiState.error ?: "Location required for search. Check GPS.",
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
    } else {
        Text(
            "Location: Ready (${String.format("%.2f", uiState.currentLocation!!.latitude)}, ...)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Extension to check location readiness
fun Location?.isReady(): Boolean {
    // Check if the location object is non-null and has valid coordinates (beyond 0,0)
    return this != null && (this.latitude != 0.0 || this.longitude != 0.0)
}