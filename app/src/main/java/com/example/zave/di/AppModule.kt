package com.example.zave.di

import android.content.Context
import androidx.room.Room
import com.example.zave.data.LocationProvider
import com.example.zave.data.local.dao.SearchHistoryDao
import com.example.zave.data.local.dao.UserDao
import com.example.zave.data.local.database.AppDatabase
import com.example.zave.data.remote.api.GooglePlacesApiService
import com.example.zave.data.remote.firebase.RemoteConfigService
import com.example.zave.data.repository.AuthRepository
import com.example.zave.data.repository.PlacesRepository
import com.example.zave.data.repository.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


//hilt module for providing DB, firebase, repositories
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Database Providers ---


    //singleton instance of the rdb
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    //dao for user data
    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()


    //dao for search history data
    @Provides
    fun provideSearchHistoryDao(db: AppDatabase): SearchHistoryDao = db.searchHistoryDao()


    // --- Firebase & System Providers ---


    //firebase authentication instance
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()


    //firebase remote config instance
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()


    // --- Repository Providers ---

    //authrepository
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        userDao: UserDao
    ): AuthRepository {
        return AuthRepository(auth, userDao)
    }


    //settingsRepository
    @Provides
    @Singleton
    fun provideSettingsRepository(
        remoteConfigService: RemoteConfigService
    ): SettingsRepository {
        return SettingsRepository(remoteConfigService)
    }


    //placerepository
    @Provides
    @Singleton
    fun providePlacesRepository(
        placesApiService: GooglePlacesApiService,
        searchHistoryDao: SearchHistoryDao,
        apiKey: String
    ): PlacesRepository {
        return PlacesRepository(placesApiService, searchHistoryDao, apiKey)
    }
}