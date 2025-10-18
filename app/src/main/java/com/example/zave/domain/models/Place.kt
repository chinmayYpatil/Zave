package com.example.zave.domain.models

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
