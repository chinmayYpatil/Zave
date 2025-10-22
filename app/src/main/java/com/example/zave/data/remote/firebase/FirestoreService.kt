package com.example.zave.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


 //Service class for handling all Firebase Firestore operations.
@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

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
            .snapshots()
            .map { snapshot ->
                // Map the documents to a Set of Place IDs for efficient lookups
                snapshot.documents.mapNotNull { it.id }.toSet()
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
            .await()
    }

    /**
     * Removes a Place ID from the user's saved collection.
     */
    suspend fun removeSavedPlace(userId: String, placeId: String) {
        val documentPath = "artifacts/$appId/users/$userId/saved_places/$placeId"

        firestore.document(documentPath)
            .delete()
            .await()
    }
}
