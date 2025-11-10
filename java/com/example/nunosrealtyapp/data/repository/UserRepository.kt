package com.example.nunosrealtyapp.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun getCurrentUser(): com.example.nunosrealtyapp.data.model.User? {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            userDoc.toObject(com.example.nunosrealtyapp.data.model.User::class.java)
        } else {
            null
        }
    }

    suspend fun updateUserProfile(fullName: String, companyName: String?, profileImageUri: Uri?): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Update profile in Firestore
                val updates = mutableMapOf<String, Any>(
                    "fullName" to fullName
                )

                companyName?.let {
                    updates["companyName"] = it
                }

                // Upload profile image if provided
                if (profileImageUri != null) {
                    val imageUrl = uploadProfileImage(profileImageUri, currentUser.uid)
                    updates["profileImageUrl"] = imageUrl
                }

                db.collection("users").document(currentUser.uid).update(updates).await()

                // Update Firebase Auth display name
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                currentUser.updateProfile(profileUpdates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadProfileImage(imageUri: Uri, userId: String): String {
        val fileName = "profile_images/$userId"
        val ref = storage.reference.child(fileName)
        val uploadTask = ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    fun logout() {
        auth.signOut()
    }
}