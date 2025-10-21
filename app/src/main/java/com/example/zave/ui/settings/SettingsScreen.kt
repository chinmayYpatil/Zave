package com.example.zave.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.* // FIX: Wildcard import added
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.zave.data.remote.firebase.RemoteConfigService
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class) // FIX: OptIn for Scaffold/TopAppBar
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // 1. Search Radius Setting
            SearchRadiusSetting(uiState = uiState, onRadiusChange = viewModel::setCustomRadius)

            Divider(Modifier.padding(vertical = 16.dp))

            // 2. Location Toggle
            LocationToggleSetting(uiState = uiState, onToggle = viewModel::toggleUseAutoLocation)

            Divider(Modifier.padding(vertical = 16.dp))

            // 3. Remote Config Debugging Section
            RemoteConfigDebugSection(uiState = uiState)
        }
    }
}

// --- Setting Composable Functions ---

@Composable
fun SearchRadiusSetting(uiState: SettingsUiState, onRadiusChange: (Double) -> Unit) {
    val maxRadius = 10f // Maximum search radius limit (e.g., 10 km)

    Text(
        text = "Search Radius Override",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    // Display current and default radius
    Text(
        text = "Current Radius: ${String.format("%.1f", uiState.customRadiusKm)} km (Default: ${uiState.remoteDefaultRadiusKm} km)",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Slider(
        value = uiState.customRadiusKm.toFloat(),
        onValueChange = { newValue ->
            // Update value: round to one decimal place for cleaner display/storage
            onRadiusChange(String.format("%.1f", newValue).toDouble())
        },
        valueRange = 1f..maxRadius,
        steps = (maxRadius * 10).roundToInt() - 2, // Steps for 0.1 increments
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LocationToggleSetting(uiState: SettingsUiState, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Use Automatic Location", style = MaterialTheme.typography.titleMedium)
            Text(
                "When disabled, location must be manually input (requires manual implementation).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = uiState.useAutoLocation,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun RemoteConfigDebugSection(uiState: SettingsUiState) {
    Text(
        text = "Firebase Remote Config (Debug Info)",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DebugText(label = RemoteConfigService.DEFAULT_RADIUS_KEY, value = "${uiState.remoteDefaultRadiusKm} km")
            DebugText(label = RemoteConfigService.FEATURED_CATEGORY_KEY, value = uiState.remoteFeaturedCategory)
            DebugText(label = RemoteConfigService.BANNER_MESSAGE_KEY, value = uiState.remoteBannerMessage)
        }
    }
}

@Composable
fun DebugText(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}