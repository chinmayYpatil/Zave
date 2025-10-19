package com.example.zave.data.repository

import com.example.zave.data.local.dao.UserDao
import com.example.zave.data.local.models.UserEntity
import com.example.zave.domain.models.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


//repository handling for user auth with firebase auth
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao // Injected Room DAO
) {
    //checking user is logged in by firebase auth
    val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    //signin with google
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

            //step 4->returning the clean domain model
            Result.success(userEntity.toDomainUser())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    //signs out the current user out of firebase and clear local storage.
    suspend fun signOut() {
        firebaseAuth.signOut()
        userDao.deleteUser()
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