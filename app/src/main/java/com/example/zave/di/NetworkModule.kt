package com.example.zave.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.zave.BuildConfig
import com.example.zave.data.remote.api.GooglePlacesApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.MessageDigest
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

    /**
     * Helper function to calculate the SHA-1 fingerprint of the app's signing certificate.
     * This is required for Google Places API key application restrictions.
     */
    private fun getSignatureSha1(context: Context): String? {
        return try {
            val packageName = context.packageName
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            // Use the first signature
            signatures?.firstOrNull()?.let { signature ->
                val md = MessageDigest.getInstance("SHA1")
                md.update(signature.toByteArray())
                // Format the hash as a colon-separated hexadecimal string (e.g., 00:AB:CD:...)
                return md.digest().joinToString(":") { "%02X".format(it) }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // OkHttpClient setting up logging for debugging
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context // Inject Context for fetching package info
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        // Calculate the SHA-1 fingerprint once
        val sha1Fingerprint = getSignatureSha1(context)

        return OkHttpClient.Builder()
            .addInterceptor(logging)

            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("X-Android-Package", context.packageName)
                    .addHeader("Referer", "android-app://${context.packageName}")

                // Only add X-Android-Cert if successfully calculated
                if (sha1Fingerprint != null) {
                    requestBuilder.addHeader("X-Android-Cert", sha1Fingerprint)
                }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    // PROVIDES MOSHI
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        // Essential: Add KotlinJsonAdapterFactory to correctly handle Kotlin data classes
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }


    // Retrofit instance for the Maps API.
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // Use the properly configured Moshi instance
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Retrofit implementation of the GooglePlacesApiService.
    @Provides
    @Singleton
    fun provideGooglePlacesApiService(retrofit: Retrofit): GooglePlacesApiService {
        return retrofit.create(GooglePlacesApiService::class.java)
    }
}