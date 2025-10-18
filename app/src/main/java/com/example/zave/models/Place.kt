package com.example.zave.models

//Data models for Places
data class Place(
    val id:String,
    val name: String,
    val vicinity: String,
    val lat: Double,
    val lng:Double,
    val rating: Double?,
    val iconUrl:String?,
    val distanceMeters: Int?=null//will be calculating in use case
)

//Data models for authentication of users
data class User(
    val uid:String,
    val name: String?,
    val email:String?,
    val photoUrl:String?
)

//Data Model for recent Search entry
data class SearchHistoryItem(
    val query: String,
    val timestamp:Long
)
