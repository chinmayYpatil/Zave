package com.example.zave.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zave.ui.theme.*

@Composable
fun RecentSearchesSection(
    searches: List<com.example.zave.domain.models.SearchHistoryItem>,
    onSearchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Recent Searches",
            style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(searches.take(5)) { item ->
                SuggestionChip(
                    onClick = { onSearchClick(item.query) },
                    label = { Text(item.query, color = TextPrimary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = CardBackground,
                        labelColor = TextPrimary
                    )
                )
            }
        }
    }
}