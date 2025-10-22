package com.example.zave.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room entity representing a cached search result place
@Entity(tableName = "place_cache")
data class PlaceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val vicinity: String,
    val lat: Double,
    val lng: Double,
    val rating: Double?,
    val iconUrl: String?,
    val distanceMeters: Int?,
    val timestamp: Long = System.currentTimeMillis() // To track freshness
) {
    fun toDomainPlace(): com.example.zave.domain.models.Place {
        return com.example.zave.domain.models.Place(
            id = this.id,
            name = this.name,
            vicinity = this.vicinity,
            lat = this.lat,
            lng = this.lng,
            rating = this.rating,
            iconUrl = this.iconUrl,
            distanceMeters = this.distanceMeters
        )
    }
}