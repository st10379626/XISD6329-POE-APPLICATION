package com.example.nunosrealtyapp.data.repository

import android.net.Uri
import com.example.nunosrealtyapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: String,
        companyName: String? = null,
        companyDocUri: Uri? = null
    ): Result<User> {
        return try {
            // 1️⃣ Create Firebase Auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // 2️⃣ Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // 3️⃣ Upload company document if agent
            var companyDocUrl: String? = null
            if (role == "agent" && companyDocUri != null) {
                val storageRef = FirebaseStorage.getInstance()
                    .reference.child("company_docs/${firebaseUser.uid}/${companyDocUri.lastPathSegment}")
                storageRef.putFile(companyDocUri).await()
                companyDocUrl = storageRef.downloadUrl.await().toString()
            }

            // 4️⃣ Create User object
            val user = User(
                id = firebaseUser.uid,
                fullName = fullName,
                email = email,
                role = role,
                companyName = if (role == "agent") companyName else null,
                companyDocUrl = if (role == "agent") companyDocUrl else null
            )

            // 5️⃣ Save user to Firestore
            db.collection("users").document(user.id).set(user).await()


            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }



    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)
                Result.success(user!!)
            } else {
                Result.failure(Exception("User data not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            User(
                id = firebaseUser.uid,
                fullName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: ""
            )
        } else {
            null
        }
    }
}
