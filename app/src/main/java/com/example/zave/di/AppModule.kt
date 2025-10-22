package com.example.zave.di

import android.content.Context
import androidx.room.Room
import com.example.zave.data.LocationProvider
import com.example.zave.data.local.dao.SearchHistoryDao
import com.example.zave.data.local.dao.UserDao
import com.example.zave.data.local.dao.PlaceDao
import com.example.zave.data.local.database.AppDatabase
import com.example.zave.data.remote.api.GooglePlacesApiService
import com.example.zave.data.remote.firebase.RemoteConfigService
import com.example.zave.data.remote.firebase.FirestoreService // ADDED
import com.example.zave.data.repository.AuthRepository
import com.example.zave.data.repository.PlacesRepository
import com.example.zave.data.repository.SettingsRepository
import com.example.zave.data.repository.SavedPlacesRepository // ADDED
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.firestore.FirebaseFirestore // ADDED
import com.google.firebase.firestore.firestore
import com.squareup.moshi.Moshi
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
        ).fallbackToDestructiveMigration()
            .build()
    }

    //dao for user data
    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()


    //dao for search history data
    @Provides
    fun provideSearchHistoryDao(db: AppDatabase): SearchHistoryDao = db.searchHistoryDao()

    //dao for place data (NEW)
    @Provides
    fun providePlaceDao(db: AppDatabase): PlaceDao = db.placeDao()


    // --- Firebase & System Providers ---


    //firebase authentication instance
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    //firebase firestore instance (ADDED)
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore


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
        remoteConfigService: RemoteConfigService,
        moshi: Moshi // Inject Moshi
    ): SettingsRepository {
        return SettingsRepository(remoteConfigService, moshi)
    }

    //placerepository
    @Provides
    @Singleton
    fun providePlacesRepository(
        placesApiService: GooglePlacesApiService,
        searchHistoryDao: SearchHistoryDao,
        placeDao: PlaceDao,
        apiKey: String
    ): PlacesRepository {
        return PlacesRepository(placesApiService, searchHistoryDao, placeDao, apiKey)
    }

    // SavedPlacesRepository (ADDED)
    @Provides
    @Singleton
    fun provideSavedPlacesRepository(
        firestoreService: FirestoreService,
        authRepository: AuthRepository
    ): SavedPlacesRepository {
        return SavedPlacesRepository(firestoreService, authRepository)
    }
}
