package com.example.nunosrealtyapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ApplicationRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun getAgentApplications(agentId: String): List<com.example.nunosrealtyapp.data.model.Application> {
        val snapshot = db.collection("applications")
            .whereEqualTo("agentId", agentId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            val application = document.toObject(com.example.nunosrealtyapp.data.model.Application::class.java)
            application?.copy(id = document.id)
        }
    }

    suspend fun updateApplicationStatus(applicationId: String, status: String): Result<Unit> {
        return try {
            db.collection("applications")
                .document(applicationId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}