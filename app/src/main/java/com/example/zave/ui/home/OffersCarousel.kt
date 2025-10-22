package com.example.zave.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zave.data.repository.Offer
import com.example.zave.ui.theme.*

@Composable
fun OffersCarousel(
    offers: List<Offer>,
    onOfferClick: (String) -> Unit, // Action to trigger a search
    modifier: Modifier = Modifier
) {
    if (offers.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            "Nearby Offers & Deals",
            style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(offers) { offer ->
                OfferCard(offer = offer, onClick = { onOfferClick(offer.relatedQuery) })
            }
        }
    }
}

@Composable
fun OfferCard(
    offer: Offer,
    onClick: () -> Unit
) {
    // Determine the background color, falling back to a themed purple if hex is invalid/missing
    val offerColor = offer.colorHex.toComposeColor(AccentPurple)

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = offerColor.copy(alpha = 0.2f) // Use a subtle background tint
        ),
        border = CardDefaults.outlinedCardBorder(
            enabled = true,

        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(offerColor.copy(alpha = 0.05f)) // Inner slightly darker tint
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.LocalOffer,
                contentDescription = null,
                tint = offerColor,
                modifier = Modifier.size(32.dp).padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    offer.title.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = offerColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    offer.description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
