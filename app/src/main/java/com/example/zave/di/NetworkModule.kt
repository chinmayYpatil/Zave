package com.example.zave.di

import com.example.zave.BuildConfig
import com.example.zave.data.remote.api.GooglePlacesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

//Hilt Module for providing network-related dependencies
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://maps.googleapis.com/"


    @Provides
    fun provideGooglePlacesApiKey(): String {
        return BuildConfig.GOOGLE_PLACES_API_KEY
    }


     //OkHttpClient setting up logging for debugging
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    //Retrofit instance for the Maps API.
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    // Retrofit implementation of the GooglePlacesApiService.
    @Provides
    @Singleton
    fun provideGooglePlacesApiService(retrofit: Retrofit): GooglePlacesApiService {
        return retrofit.create(GooglePlacesApiService::class.java)
    }
}