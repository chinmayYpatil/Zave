package com.example.zave.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zave.data.LocationProvider
import com.example.zave.data.local.dao.SearchHistoryDao
import com.example.zave.data.repository.SettingsRepository
import com.example.zave.data.repository.RemoteCategory
import com.example.zave.data.repository.Offer
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
    val categoryItems: List<RemoteCategory> = emptyList(),
    val nearbyOffers: List<Offer> = emptyList(),
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

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Flow to hold the user's search query input from the TextField
    private val _queryInput = MutableStateFlow("")
    val queryInput: StateFlow<String> = _queryInput.asStateFlow()

    init {
        // Start background tasks immediately
        viewModelScope.launch {
            // 1. Collect remote config and search history updates
            collectRemoteConfigAndHistory()
        }
        // 2. Start initial data fetch/refresh
        refreshData()
    }


    //Collects data streams for reactive updates to the Home UI.
    private fun collectRemoteConfigAndHistory() = viewModelScope.launch {
        searchHistoryDao.getRecentQueries().collect { entities ->
            val domainItems = entities.map {
                SearchHistoryItem(query = it.query, timestamp = it.timestamp)
            }
            _uiState.update { it.copy(recentSearches = domainItems) }
        }
    }

    //Central function to fetch location and update remote config.
    fun refreshData() = viewModelScope.launch {
        // Start loading
        _uiState.update { it.copy(isLoading = true, error = null) }

        // 1. Fetch remote config
        val remoteConfigSuccess = settingsRepository.initialize()

        // 2. Update UI state with remote config values regardless of success (to use cached values if fetch fails)
        updateRemoteConfigValues()

        if (!remoteConfigSuccess) {
            // Log or handle failure to fetch/activate remote config if needed
        }

        // 3. Request current location
        requestLocation() // This will update isLoading back to false when complete
    }


    // Updates the UI state with the current values from the SettingsRepository
    private fun updateRemoteConfigValues() {
        _uiState.update {
            it.copy(
                bannerMessage = settingsRepository.getBannerMessage(),
                featuredCategory = settingsRepository.getFeaturedCategory(),
                categoryItems = settingsRepository.getCategoryItems(),
                nearbyOffers = settingsRepository.getNearbyOffers(), // ADDED: Fetch offers
                // Keep isLoading true until location is fetched
            )
        }
    }


    //Updates the text input field state.
    fun updateQueryInput(newQuery: String) {
        _queryInput.value = newQuery
    }

    //fetch the user's location
    fun requestLocation() = viewModelScope.launch {
        _uiState.update { it.copy(error = null) }

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
                    error = "Could not get location. Check GPS settings."
                )
            }
        }
    }

    fun setPermissionRequired(required: Boolean) {
        _uiState.update { it.copy(requiresLocationPermission = required, isLoading = false) }
    }

    fun getEffectiveSearchQuery(): String {
        val input = _queryInput.value.trim()
        return if (input.isNotEmpty()) input else uiState.value.featuredCategory
    }
}
