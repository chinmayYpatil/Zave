package com.example.zave.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zave.data.local.dao.SearchHistoryDao
import com.example.zave.data.local.dao.UserDao
import com.example.zave.data.local.dao.PlaceDao
import com.example.zave.data.local.models.SearchQueryEntity
import com.example.zave.data.local.models.UserEntity
import com.example.zave.data.local.models.PlaceEntity

//main db class for the application
@Database(
    entities = [UserEntity::class, SearchQueryEntity::class, PlaceEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun placeDao(): PlaceDao

    companion object {
        const val DATABASE_NAME = "Zave_db"
    }
}