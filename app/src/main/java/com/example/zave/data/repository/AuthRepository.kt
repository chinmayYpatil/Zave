package com.example.zave.data.repository

import com.example.zave.data.local.dao.UserDao
import com.example.zave.data.local.models.UserEntity
import com.example.zave.domain.models.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


//repository handling for user auth with firebase auth
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao // Injected Room DAO
) {
    // StateFlow to track the current user's UID (null if signed out)
    private val _currentUserId = MutableStateFlow(firebaseAuth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        // Listen for Auth state changes to keep the flow updated
        firebaseAuth.addAuthStateListener { auth ->
            _currentUserId.value = auth.currentUser?.uid
        }
    }

    // checking user is logged in by firebase auth
    val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    // Helper to get the ID synchronously (useful for one-off calls)
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    // signin with google
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> {
        return try {
            val idToken = account.idToken ?: throw Exception("Google ID token is null.")

            //step 1->google id token for firebase credential
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            //step 2->sign in to firebase
            val firebaseUser = firebaseAuth.signInWithCredential(credential).await().user
                ?: throw Exception("Firebase sign-in failed.")

            //step 3->storing the user data locally
            val userEntity = UserEntity(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName,
                email = firebaseUser.email,
                photoUrl = firebaseUser.photoUrl?.toString()
            )
            userDao.insertUser(userEntity)

            _currentUserId.value = firebaseUser.uid // Update flow

            //step 4->returning the clean domain model
            Result.success(userEntity.toDomainUser())

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    // signs out the current user out of firebase and clear local storage.
    suspend fun signOut() {
        firebaseAuth.signOut()
        userDao.deleteUser()
        _currentUserId.value = null // Update flow
    }

    //helper functions
    private fun UserEntity.toDomainUser(): User {
        return User(
            uid = this.uid,
            name = this.name,
            email = this.email,
            photoUrl = this.photoUrl
        )
    }
}
