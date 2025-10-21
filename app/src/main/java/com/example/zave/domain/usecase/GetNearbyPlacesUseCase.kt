package com.example.zave.domain.usecase

import com.example.zave.data.repository.PlacesRepository
import com.example.zave.domain.models.Place
import javax.inject.Inject


//buisness logic of fetching nearby store
class GetNearbyPlacesUseCase @Inject constructor(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(
        query: String,
        radiusKm: Double,
        userLat: Double,
        userLng: Double
    ): Result<List<Place>> {
        // 1. Update the repository with the current location
        placesRepository.setCurrentLocation(userLat, userLng)

        // 2. Convert radius from km to Meters-required by API
        val radiusMeters = (radiusKm * 1000).toInt()

        // 3. Execute the search via the repository
        val result = placesRepository.getNearbyPlaces(
            keyword = query,
            radiusMeters = radiusMeters
        )
        // 4. Return the result. Sorting and mapping are handled by the Repository.
        return result
    }
}