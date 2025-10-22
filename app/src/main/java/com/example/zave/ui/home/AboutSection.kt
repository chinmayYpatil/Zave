package com.example.zave.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zave.ui.theme.*

@Composable
fun AboutSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "ABOUT US",
            style = MaterialTheme.typography.labelLarge.copy(
                color = AccentBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Zave is on a mission to provide you the most informed and fast shopping experience. Find stores near you and discover the best deals!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextPrimary,
                lineHeight = 24.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Made with", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
            Spacer(modifier = Modifier.width(6.dp))
            Text("❤️", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(6.dp))
            Text("in India", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        }
    }
}