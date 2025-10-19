package com.example.zave.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey

//Room Entity for storing the authenticated user's details locally
@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val name: String?,
    val email: String?,
    val photoUrl: String?
)