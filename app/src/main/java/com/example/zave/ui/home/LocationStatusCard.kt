package com.example.zave.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.zave.ui.theme.*


@Composable
fun LocationStatusCard(
    uiState: HomeUiState,
    onPermissionRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText: String
    val statusColor: Color
    val icon: ImageVector

    when {
        uiState.requiresLocationPermission -> {
            statusText = "Location permission required. Tap to grant access."
            statusColor = Color(0xFFFF6B6B)
            icon = Icons.Default.LocationOff
        }
        else -> {
            statusText = "Fetching your location..."
            statusColor = AccentBlue
            icon = Icons.Default.LocationSearching
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = uiState.requiresLocationPermission) { onPermissionRequest() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                statusText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary
                )
            )
        }
    }
}