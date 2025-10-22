package com.example.zave.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.zave.data.local.models.PlaceEntity

// DAO for managing cached place results
@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<PlaceEntity>)

    // For a simple cache, we clear the previous results before inserting new ones
    @Query("DELETE FROM place_cache")
    suspend fun clearCache()

    // Retrieve all cached places, ordered by recency of insertion
    @Query("SELECT * FROM place_cache ORDER BY timestamp DESC")
    suspend fun getAllCachedPlaces(): List<PlaceEntity>

    // Utility function to clear and insert atomically
    @Transaction
    suspend fun updateCache(places: List<PlaceEntity>) {
        clearCache()
        insertPlaces(places)
    }
}