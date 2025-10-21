package com.example.zave.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zave.data.LocationProvider
import com.example.zave.data.local.dao.SearchHistoryDao
import com.example.zave.data.repository.SettingsRepository
import com.example.zave.domain.models.SearchHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class to represent the different states of the Home Screen UI
data class HomeUiState(
    val bannerMessage: String = "",
    val featuredCategory: String = "",
    val searchInput: String = "",
    val recentSearches: List<SearchHistoryItem> = emptyList(),
    val currentLocation: Location? = null,
    val isLoading: Boolean = true,
    val requiresLocationPermission: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val searchHistoryDao: SearchHistoryDao,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Flow to hold the user's search query input from the TextField
    private val _queryInput = MutableStateFlow("")
    val queryInput: StateFlow<String> = _queryInput.asStateFlow()

    init {
        // Start background tasks immediately
        viewModelScope.launch {
            // 1. Initialize settings (fetches remote config)
            settingsRepository.initialize()

            // 2. Collect remote config and search history updates
            collectRemoteConfigAndHistory()

            // 3. Attempt to get location (permission must be requested in Activity)
            //    We will call requestLocation() later once permission is confirmed
        }
    }

    /**
     * Collects data streams for reactive updates to the Home UI.
     */
    private fun collectRemoteConfigAndHistory() = viewModelScope.launch {
        // Collect Search History from Room DAO (always takes the last 5)
        searchHistoryDao.getRecentQueries().collect { entities ->
            val domainItems = entities.map {
                SearchHistoryItem(query = it.query, timestamp = it.timestamp)
            }
            _uiState.update { it.copy(recentSearches = domainItems) }
        }
    }

    /**
     * Sets the initial remote config values after fetch is complete.
     */
    fun setRemoteConfigInitialValues() {
        _uiState.update {
            it.copy(
                bannerMessage = settingsRepository.getBannerMessage(),
                featuredCategory = settingsRepository.getFeaturedCategory(),
                isLoading = false
            )
        }
    }

    /**
     * Updates the text input field state.
     */
    fun updateQueryInput(newQuery: String) {
        _queryInput.value = newQuery
    }

    /**
     * Attempts to fetch the user's location. Should only be called AFTER permission is granted.
     */
    fun requestLocation() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        val location = locationProvider.getCurrentLocation()

        if (location != null) {
            _uiState.update {
                it.copy(
                    currentLocation = location,
                    isLoading = false,
                    requiresLocationPermission = false,
                    error = null
                )
            }
        } else {
            // Location could not be fetched (e.g., GPS is off)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    // If location is null but permission was granted, show an error message
                    error = "Could not get location. Check GPS settings."
                )
            }
        }
    }

    /**
     * Sets the state to indicate location permission is required.
     */
    fun setPermissionRequired(required: Boolean) {
        _uiState.update { it.copy(requiresLocationPermission = required, isLoading = false) }
    }

    /**
     * Returns the final query (either user input or featured category).
     */
    fun getEffectiveSearchQuery(): String {
        val input = _queryInput.value.trim()
        return if (input.isNotEmpty()) input else uiState.value.featuredCategory
    }
}