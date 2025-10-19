package com.example.zave.data.repository

import com.example.zave.data.remote.firebase.RemoteConfigService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

//repository to manage application settings
class SettingsRepository @Inject constructor(
    private val remoteConfigService: RemoteConfigService
) {
    private val _customRadiusKm = MutableStateFlow(remoteConfigService.getDefaultRadiusKm())
    val customRadiusKm: StateFlow<Double> = _customRadiusKm.asStateFlow()

    private val _useAutoLocation = MutableStateFlow(true)
    val useAutoLocation: StateFlow<Boolean> = _useAutoLocation.asStateFlow()


    suspend fun initialize() {
        remoteConfigService.fetchAndActivate()

        _customRadiusKm.value = remoteConfigService.getDefaultRadiusKm()
    }


    fun getFeaturedCategory(): String {
        return remoteConfigService.getFeaturedCategory()
    }

    fun getBannerMessage(): String {
        return remoteConfigService.getBannerMessage()
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