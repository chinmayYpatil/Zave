package com.example.zave.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.tasks.await // FIX: Added KTX import for await()
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for handling all Firebase Firestore operations.
 * Saved places are stored in the private user collection:
 * /artifacts/{appId}/users/{userId}/saved_places/{placeId}
 */
@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // FIX: Using a placeholder app ID since the global variable __app_id is unavailable/unresolved
    private val appId = "default-zave-app-id"

    // Data transfer object for saving a Place's ID in Firestore
    data class SavedPlaceDto(
        val placeId: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Gets a Flow of all saved Place IDs for the current user.
     * @param userId The UID of the currently authenticated user.
     */
    fun getSavedPlaceIds(userId: String): Flow<Set<String>> {
        val collectionPath = "artifacts/$appId/users/$userId/saved_places"

        return firestore.collection(collectionPath)
            .snapshots() // FIX: resolved by import
            .map { snapshot ->
                // Map the documents to a Set of Place IDs for efficient lookups
                snapshot.documents.mapNotNull { it.id }.toSet() // FIX: documents and it.id are correct
            }
    }

    /**
     * Adds a Place ID to the user's saved collection.
     * The document ID is the placeId itself.
     */
    suspend fun addSavedPlace(userId: String, placeId: String) {
        val documentPath = "artifacts/$appId/users/$userId/saved_places/$placeId"

        val dto = SavedPlaceDto(placeId = placeId)
        firestore.document(documentPath)
            .set(dto, SetOptions.merge())
            .await() // FIX: resolved by import
    }

    /**
     * Removes a Place ID from the user's saved collection.
     */
    suspend fun removeSavedPlace(userId: String, placeId: String) {
        val documentPath = "artifacts/$appId/users/$userId/saved_places/$placeId"

        firestore.document(documentPath)
            .delete()
            .await() // FIX: resolved by import
    }
}
