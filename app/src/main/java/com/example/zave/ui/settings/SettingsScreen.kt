package com.example.zave.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.zave.data.remote.firebase.RemoteConfigService
import com.example.zave.ui.common.navigation.Screen
import com.example.zave.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Function to handle sign out and navigation
    val onSignOut = {
        viewModel.signOut()
        navController.navigate(Screen.Auth.route) {
            // Clear the back stack so the user can't navigate back to Home/Settings
            popUpTo(Screen.Home.route) { inclusive = true }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
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


            Divider(Modifier.padding(vertical = 16.dp), color = TextSecondary.copy(alpha = 0.3f))

            Divider(Modifier.padding(vertical = 16.dp), color = TextSecondary.copy(alpha = 0.3f))

            // 2. Remote Config Debugging Section
            RemoteConfigDebugSection(uiState = uiState)

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Sign Out Button (ADDED)
            SignOutButton(onSignOut = onSignOut)
        }
    }
}

// --- New Sign Out Composable ---

@Composable
fun SignOutButton(onSignOut: () -> Unit) {
    Button(
        onClick = onSignOut,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
    ) {
        Icon(
            Icons.Default.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out")
    }
}


// --- Setting Composable Functions ---

@Composable
fun SearchRadiusSetting(uiState: SettingsUiState, onRadiusChange: (Double) -> Unit) {
    val maxRadius = 10f // Maximum search radius limit (e.g., 10 km)

    Text(
        text = "Search Radius Override",
        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
        modifier = Modifier.padding(bottom = 8.dp)
    )

    // Display current and default radius
    Text(
        text = "Current Radius: ${String.format("%.1f", uiState.customRadiusKm)} km (Default: ${uiState.remoteDefaultRadiusKm} km)",
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
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
        modifier = Modifier.fillMaxWidth(),
        colors = SliderDefaults.colors(
            thumbColor = AccentBlue,
            activeTrackColor = AccentBlue,
            inactiveTrackColor = TextSecondary.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun RemoteConfigDebugSection(uiState: SettingsUiState) {
    Text(
        text = "Firebase Remote Config (Debug Info)",
        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
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
            color = TextSecondary,
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
        )
    }
}
