package com.example.zave.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zave.data.local.models.SearchQueryEntity
import kotlinx.coroutines.flow.Flow

//dao for managing users search history
@Dao
interface SearchHistoryDao {

    //will insert a new search query. if the query already exists it will replace the old one.
    //in order to replace the most recent search history
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(query: SearchQueryEntity)

    //retrieving last 5 unique search query
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 5")
    fun getRecentQueries(): Flow<List<SearchQueryEntity>>

    //clears all saved search history
    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}