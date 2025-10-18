package com.example.zave.domain.models

//Data models for authentication of users
data class User(
    val uid:String,
    val name: String?,
    val email:String?,
    val photoUrl:String?
)