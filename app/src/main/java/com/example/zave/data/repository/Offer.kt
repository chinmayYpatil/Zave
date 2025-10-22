package com.example.zave.data.repository

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for parsing a single offer item from Firebase Remote Config JSON.
 * This model defines the structure of the data expected from the 'nearby_offers_json' key.
 */
@JsonClass(generateAdapter = true)
data class Offer(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "related_query") val relatedQuery: String, // Search query to run when clicked
    @Json(name = "color_hex") val colorHex: String? // Optional background color (e.g., #FF9900)
)
