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

        val DEFAULT_CONFIG = mapOf(
            DEFAULT_RADIUS_KEY to 5.0,
            FEATURED_CATEGORY_KEY to "clothing",
            BANNER_MESSAGE_KEY to "Welcome! Discover stores near you."
        )
    }

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(DEFAULT_CONFIG)
    }

    //Fetches the latest configuration values from Firebase
    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            false
        }
    }


    //Retrieves the default search radius in km
    fun getDefaultRadiusKm(): Double {
        return remoteConfig.getDouble(DEFAULT_RADIUS_KEY)
    }


    //Retrieves the featured search category string
    fun getFeaturedCategory(): String {
        return remoteConfig.getString(FEATURED_CATEGORY_KEY)
    }


    //Retrieves the banner message text
    fun getBannerMessage(): String {
        return remoteConfig.getString(BANNER_MESSAGE_KEY)
    }
}