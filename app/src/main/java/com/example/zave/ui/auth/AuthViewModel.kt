package com.example.zave.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zave.data.repository.AuthRepository
import com.example.zave.domain.models.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class to represent the different states of the authentication flow
sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check initial state: if already authenticated, immediately set success.
        if (authRepository.isAuthenticated) {
            // Note: In a real app, you would fetch the full User domain model from the DAO here.
            // For simplicity, we assume if authenticated, we move to Home.
            // We use a dummy Success state to trigger navigation.
            _uiState.value = AuthUiState.Success(User("id_cached", "Cached User", null, null))
        }
    }

    /**
     * Attempts to sign the user into Firebase after receiving the GoogleSignInAccount
     * from the Activity result (via the Compose screen).
     */
    fun signInWithGoogle(account: GoogleSignInAccount) {
        if (_uiState.value == AuthUiState.Loading) return // Prevent duplicate clicks

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(account)

            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user)
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error("Login failed: ${e.message}")
            }
        }
    }

    /**
     * Clears the error state after it has been displayed to the user.
     */
    fun clearError() {
        _uiState.update { AuthUiState.Idle }
    }
}