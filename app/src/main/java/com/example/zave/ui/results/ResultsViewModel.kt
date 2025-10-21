package com.example.zave.ui.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zave.data.LocationProvider
import com.example.zave.data.repository.SettingsRepository
import com.example.zave.domain.models.Place
import com.example.zave.domain.usecase.GetNearbyPlacesUseCase
import com.example.zave.ui.common.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class to represent the different states of the Results Screen
sealed class ResultsUiState {
    data object Loading : ResultsUiState()
    data class Success(val places: List<Place>) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
    data object Empty : ResultsUiState()
}

@HiltViewModel
class ResultsViewModel @Inject constructor(
    // Retrieves navigation arguments (the search query)
    savedStateHandle: SavedStateHandle,
    private val getNearbyPlacesUseCase: GetNearbyPlacesUseCase,
    private val settingsRepository: SettingsRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val query: String = savedStateHandle[Screen.Results.ARG_QUERY] ?: ""

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace: StateFlow<Place?> = _selectedPlace.asStateFlow()

    init {
        // Execute the search immediately when the ViewModel is created
        startSearch()
    }

    private fun startSearch() = viewModelScope.launch {
        _uiState.value = ResultsUiState.Loading

        // 1. Get location and radius
        val location = locationProvider.getCurrentLocation()
        val radius = settingsRepository.getEffectiveSearchRadiusKm()

        if (location == null) {
            _uiState.value = ResultsUiState.Error("Location not available. Cannot perform search.")
            return@launch
        }

        // 2. Execute the Use Case
        val result = getNearbyPlacesUseCase(
            query = query,
            radiusKm = radius,
            userLat = location.latitude,
            userLng = location.longitude
        )

        // 3. Update state based on result
        result.onSuccess { places ->
            if (places.isEmpty()) {
                _uiState.value = ResultsUiState.Empty
            } else {
                _uiState.value = ResultsUiState.Success(places)
            }
        }.onFailure { e ->
            _uiState.value = ResultsUiState.Error("Search failed: ${e.message}")
            // *Future enhancement: Fallback to local cache here*
        }
    }

    fun selectPlace(place: Place?) {
        _selectedPlace.value = place
    }
}