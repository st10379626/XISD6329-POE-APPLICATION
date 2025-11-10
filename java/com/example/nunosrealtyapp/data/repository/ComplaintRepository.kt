package com.example.nunosrealtyapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ComplaintRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun getUserComplaints(userId: String): List<com.example.nunosrealtyapp.data.model.Complaint> {
        val snapshot = db.collection("complaints")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(com.example.nunosrealtyapp.data.model.Complaint::class.java)
        }
    }

    suspend fun submitComplaint(userId: String, subject: String, message: String): Result<String> {
        return try {
            val complaint = com.example.nunosrealtyapp.data.model.Complaint(
                userId = userId,
                subject = subject,
                message = message,
                status = "pending",
                createdAt = java.util.Date()
            )

            val document = db.collection("complaints").document()
            val complaintWithId = complaint.copy(id = document.id)
            document.set(complaintWithId).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}