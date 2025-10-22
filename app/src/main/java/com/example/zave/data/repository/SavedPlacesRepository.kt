// zave/data/repository/SavedPlacesRepository.kt
package com.example.zave.data.repository


import com.example.zave.data.remote.firebase.FirestoreService
import com.example.zave.domain.models.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
// ADDED: Import flow builder and flatMapLatest operator
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Repository for managing the user's list of saved (favorited) places using Firestore.
 */
class SavedPlacesRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authRepository: AuthRepository
) {
    /**
     * Provides a stream of Place IDs saved by the current user.
     * Returns an empty set flow if the user is not authenticated.
     */
    val savedPlaceIds: Flow<Set<String>> = authRepository.currentUserId
        // FIX: Use flatMapLatest to flatten the Flow<Flow<T>> into Flow<T>
        .flatMapLatest { userId ->
            // If the user is authenticated, start listening to Firestore.
            // If not authenticated or ID is null, return an empty set.
            userId?.let {
                firestoreService.getSavedPlaceIds(it)
            } ?: flow { emit(emptySet()) } // FIX: Use the flow builder function
        }
    // REMOVED: The subsequent .map { it } is no longer needed

    /**
     * Toggles the saved status of a place.
     */
    suspend fun toggleSavedStatus(place: Place, isCurrentlySaved: Boolean) {
        val userId = authRepository.getCurrentUserId() ?: return // Must have a user ID

        if (isCurrentlySaved) {
            firestoreService.removeSavedPlace(userId, place.id)
        } else {
            firestoreService.addSavedPlace(userId, place.id)
        }
    }
}