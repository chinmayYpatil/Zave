package com.example.zave.data.remote.firebase

import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

//Service class for fetching and managing parameters from Firebase Remote Config.
class RemoteConfigService @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    companion object {
        const val DEFAULT_RADIUS_KEY = "default_radius_km"
        const val FEATURED_CATEGORY_KEY = "featured_category"
        const val BANNER_MESSAGE_KEY = "banner_message"
        // Key for the category list JSON
        const val CATEGORIES_JSON_KEY = "category_cards_json"

        // DEFAULT_CONFIG map is REMOVED.
    }

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // remoteConfig.setDefaultsAsync is REMOVED.
    }

    //Fetches the latest configuration values from Firebase
    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            false
        }
    }


    //Retrieves the default search radius in km. Returns 0.0 if not fetched.
    fun getDefaultRadiusKm(): Double {
        return remoteConfig.getDouble(DEFAULT_RADIUS_KEY)
    }


    //Retrieves the featured search category string. Returns "" if not fetched.
    fun getFeaturedCategory(): String {
        return remoteConfig.getString(FEATURED_CATEGORY_KEY)
    }


    //Retrieves the banner message text. Returns "" if not fetched.
    fun getBannerMessage(): String {
        return remoteConfig.getString(BANNER_MESSAGE_KEY)
    }

    // Retrieves the categories list as a JSON string. Returns "" if not fetched.
    fun getCategoriesJson(): String {
        return remoteConfig.getString(CATEGORIES_JSON_KEY)
    }
}