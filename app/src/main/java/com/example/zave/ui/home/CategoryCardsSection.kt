package com.example.zave.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.zave.data.repository.RemoteCategory
import com.example.zave.ui.theme.*

@Composable
fun CategoryCardsSection(
    remoteCategories: List<RemoteCategory>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map the remote categories to the UI models
    val categoryList = remember(remoteCategories) {
        remoteCategories.map { it.toCategoryItem() }
    }

    Column(modifier = modifier) {
        Text(
            "Shop by Category",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (categoryList.isNotEmpty()) {
            LazyVerticalGrid(
                // CHANGED: Use 4 columns for a smaller, compact grid
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                // CHANGED: Reduced height significantly for 2 rows of 4
                modifier = Modifier.height(250.dp),
                userScrollEnabled = false
            ) {
                items(categoryList) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Ensures card remains square
            .clickable(onClick = onClick),
        // Use simple rounded corner and dark background style
        shape = RoundedCornerShape(12.dp),
        // FIX: Use the category's color as the container color
        colors = CardDefaults.cardColors(containerColor = category.color.copy(alpha = 0.1f)) // Use a subtle tint of the color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp), // Reduced padding for compactness
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                category.icon,
                contentDescription = null,
                tint = category.color, // Keep the icon color the full, vibrant color
                modifier = Modifier.size(32.dp) // Reduced icon size
            )
            Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing
            Text(
                category.name,
                style = MaterialTheme.typography.labelSmall.copy( // Used smaller text style
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}