package com.example.zave.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zave.data.local.dao.SearchHistoryDao
import com.example.zave.data.local.dao.UserDao
import com.example.zave.data.local.models.SearchQueryEntity
import com.example.zave.data.local.models.UserEntity

//main db class for the application
@Database(
    entities = [UserEntity::class, SearchQueryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        const val DATABASE_NAME = "Zave_db"
    }
}