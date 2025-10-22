package com.example.zave.data.repository

import com.example.zave.data.local.dao.SearchHistoryDao
import com.example.zave.data.local.dao.PlaceDao // ADDED
import com.example.zave.data.local.models.SearchQueryEntity
import com.example.zave.data.remote.api.GooglePlacesApiService
import com.example.zave.data.remote.api.PlaceDto
import com.example.zave.domain.models.Place
import com.example.zave.data.local.models.PlaceEntity // ADDED
import javax.inject.Inject
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


//repository for handling place search operations
class PlacesRepository @Inject constructor(
    private val placesApiService: GooglePlacesApiService,
    private val searchHistoryDao: SearchHistoryDao,
    private val placeDao: PlaceDao, // ADDED
    private val apiKey: String
) {
    private var currentUserLat: Double = 0.0
    private var currentUserLng: Double = 0.0


    fun setCurrentLocation(lat: Double, lng: Double) {
        currentUserLat = lat
        currentUserLng = lng
    }


    suspend fun getNearbyPlaces(
        keyword: String,
        radiusMeters: Int
    ): Result<List<Place>> {
        val location = "$currentUserLat,$currentUserLng"

        //saves the search query immediately (updates timestamp if query exists)
        searchHistoryDao.insertQuery(SearchQueryEntity(query = keyword))

        return try {
            val response = placesApiService.getNearbyPlaces(
                location = location,
                radius = radiusMeters,
                keyword = keyword,
                apikey = apiKey
            )

            if (response.isSuccessful && response.body() != null) {
                val domainPlaces = response.body()!!.results
                    .map { dto -> mapPlaceDtoToDomain(dto) }
                    .sortedBy { it.distanceMeters }

                // Cache successful results
                val entities = domainPlaces.map { it.toPlaceEntity() }
                placeDao.updateCache(entities)

                Result.success(domainPlaces)
            } else {
                // FALLBACK 1: API error (e.g., HTTP 4xx/5xx)
                val cachedPlaces = placeDao.getAllCachedPlaces().map { it.toDomainPlace() }
                if (cachedPlaces.isNotEmpty()) {
                    return Result.success(cachedPlaces) // Return cached data on soft failure
                }

                Result.failure(Exception("API Error: ${response.code()}. No cached results available."))
            }
        } catch (e: Exception) {
            // FALLBACK 2: Network error (e.g., SocketTimeoutException)
            e.printStackTrace()
            val cachedPlaces = placeDao.getAllCachedPlaces().map { it.toDomainPlace() }
            if (cachedPlaces.isNotEmpty()) {
                return Result.success(cachedPlaces) // Return cached data on hard failure
            }

            Result.failure(e)
        }
    }


    private fun mapPlaceDtoToDomain(dto: PlaceDto): Place {
        val placeLat = dto.geometry.location.lat
        val placeLng = dto.geometry.location.lng

        val distance = calculateDistance(
            currentUserLat, currentUserLng,
            placeLat, placeLng
        )

        return Place(
            id = dto.placeId,
            name = dto.name,
            vicinity = dto.vicinity,
            lat = placeLat,
            lng = placeLng,
            rating = dto.rating,
            iconUrl = dto.icon,
            distanceMeters = distance,
            openNow = dto.openingHours?.openNow
        )
    }

    // Helper extension function to map domain model to entity (to save in cache)
    private fun Place.toPlaceEntity(): PlaceEntity {
        return PlaceEntity(
            id = this.id,
            name = this.name,
            vicinity = this.vicinity,
            lat = this.lat,
            lng = this.lng,
            rating = this.rating,
            iconUrl = this.iconUrl,
            distanceMeters = this.distanceMeters,
            openNow = this.openNow
        )
    }


    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Int {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * acos(kotlin.math.min(1.0, kotlin.math.sqrt(a)))
        return (R * c).toInt()
    }
}