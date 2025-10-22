package com.example.zave.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zave.data.remote.firebase.RemoteConfigService
import com.example.zave.data.repository.AuthRepository  
import com.example.zave.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch  
import javax.inject.Inject

// Data class to combine all required settings and debug information
data class SettingsUiState(
    val customRadiusKm: Double,
    val useAutoLocation: Boolean,
    val remoteDefaultRadiusKm: Double,
    val remoteFeaturedCategory: String,
    val remoteBannerMessage: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val remoteConfigService: RemoteConfigService,
    private val authRepository: AuthRepository  
) : ViewModel() {

    // 1. Fetch static Remote Config values for debug purposes
    private val remoteDefaultRadiusKm = remoteConfigService.getDefaultRadiusKm()
    private val remoteFeaturedCategory = remoteConfigService.getFeaturedCategory()
    private val remoteBannerMessage = remoteConfigService.getBannerMessage()

    // 2. Combine reactive flows from the Repository with static debug data
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.customRadiusKm,
        settingsRepository.useAutoLocation
    ) { customRadius, useAuto ->
        SettingsUiState(
            customRadiusKm = customRadius,
            useAutoLocation = useAuto,
            remoteDefaultRadiusKm = remoteDefaultRadiusKm,
            remoteFeaturedCategory = remoteFeaturedCategory,
            remoteBannerMessage = remoteBannerMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(
            customRadiusKm = settingsRepository.getEffectiveSearchRadiusKm(),
            useAutoLocation = settingsRepository.useAutoLocation.value,
            remoteDefaultRadiusKm = remoteDefaultRadiusKm,
            remoteFeaturedCategory = remoteFeaturedCategory,
            remoteBannerMessage = remoteBannerMessage
        )
    )

    /**
     * Updates the user's custom search radius preference.
     */
    fun setCustomRadius(radius: Double) {
        settingsRepository.setCustomRadiusKm(radius)
    }

    /**
     * Updates the user's preference for automatic location usage.
     */
    fun toggleUseAutoLocation(enabled: Boolean) {
        settingsRepository.setUseAutoLocation(enabled)
    }

    /**
     * Signs out the current user and clears local data.
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
