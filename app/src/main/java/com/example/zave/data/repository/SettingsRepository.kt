package com.example.zave.data.repository

import com.example.zave.data.remote.firebase.RemoteConfigService
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Data class for parsing the remote config JSON for categories
@JsonClass(generateAdapter = true)
data class RemoteCategory(
    @Json(name = "name") val name: String,
    @Json(name = "key") val key: String, // Key to map to a Compose Icon/Color
    @Json(name = "color") val color: String? // Hex color string
)


//repository to manage application settings
class SettingsRepository @Inject constructor(
    private val remoteConfigService: RemoteConfigService,
    private val moshi: Moshi // Inject Moshi for JSON parsing
) {
    private val _customRadiusKm = MutableStateFlow(remoteConfigService.getDefaultRadiusKm())
    val customRadiusKm: StateFlow<Double> = _customRadiusKm.asStateFlow()

    private val _useAutoLocation = MutableStateFlow(true)
    val useAutoLocation: StateFlow<Boolean> = _useAutoLocation.asStateFlow()


    suspend fun initialize(): Boolean {
        val success = remoteConfigService.fetchAndActivate()

        if (success) {
            // Re-fetch custom radius in case it was updated after fetchAndActivate
            _customRadiusKm.value = remoteConfigService.getDefaultRadiusKm()
        }
        return success
    }


    fun getFeaturedCategory(): String {
        return remoteConfigService.getFeaturedCategory()
    }

    fun getBannerMessage(): String {
        return remoteConfigService.getBannerMessage()
    }

    // Function to parse and return the list of categories
    fun getCategoryItems(): List<RemoteCategory> {
        val jsonString = remoteConfigService.getCategoriesJson()

        return try {
            val listType = Types.newParameterizedType(List::class.java, RemoteCategory::class.java)
            val adapter = moshi.adapter<List<RemoteCategory>>(listType)
            adapter.fromJson(jsonString) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    fun setCustomRadiusKm(radius: Double) {
        _customRadiusKm.value = radius
        // To be implemented: userPreferences.saveRadius(radius) to persist this value
    }


    fun setUseAutoLocation(useAuto: Boolean) {
        _useAutoLocation.value = useAuto
        // To be implemented: userPreferences.saveLocationToggle(useAuto)
    }

    fun getEffectiveSearchRadiusKm(): Double {
        return _customRadiusKm.value
    }
}