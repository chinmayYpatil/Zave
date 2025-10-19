package com.example.zave.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey

//room entity representing a single entry in the search history
@Entity(tableName = "search_history")
data class SearchQueryEntity(
    @PrimaryKey
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)