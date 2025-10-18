package com.example.zave.data.remote.api

import android.R.attr.name
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

//retrofit interface for interacting with the google place api end points.
interface GooglePlacesApiService{

    //for nearbysearch to find stores based on location,radius and keywords
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("keyword") keyword:String,
        @Query("type") type: String="store",//default store type
        @Query("key") apikey: String
    ): Response<NearbySearchResponse>
}

// Root response object for the Nearby Search API call.
@JsonClass(generateAdapter = true)
data class NearbySearchResponse(
    @Json(name = "results") val results: List<PlaceDto>
)

// Data Transfer Object for a single Place.
@JsonClass(generateAdapter = true)
data class PlaceDto(
    @Json(name = "place_id") val placeId: String,
    @Json(name = "name") val name: String,
    @Json(name = "vicinity") val vicinity: String, // Address/Area description
    @Json(name = "rating") val rating: Double?,
    @Json(name = "icon") val icon: String?,
    @Json(name = "geometry") val geometry: GeometryDto
)

//Nested DTO for geographical coordinates
@JsonClass(generateAdapter = true)
data class GeometryDto(
    @Json(name = "location") val location: LocationDto
)


//Nested DTO for latitude and longitude
@JsonClass(generateAdapter = true)
data class LocationDto(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)

