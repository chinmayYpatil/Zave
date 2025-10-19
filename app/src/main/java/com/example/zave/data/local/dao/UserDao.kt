package com.example.zave.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zave.data.local.models.UserEntity


 //Dao storing and managing the authenticated user's details.
@Dao
interface UserDao {
    //insets or will replace current user info
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    //for retrieving current user info
    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun getUser(): UserEntity?

    //clears user table after logout
    @Query("DELETE FROM user_table")
    suspend fun deleteUser()
}