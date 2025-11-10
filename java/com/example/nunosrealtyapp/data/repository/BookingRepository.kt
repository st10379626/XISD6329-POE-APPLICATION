package com.example.nunosrealtyapp.data.repository

import android.util.Log
import com.example.nunosrealtyapp.data.model.Booking
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BookingRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val bookingsCollection = db.collection("bookings")

    // ================================
    // Fetch all pending bookings (any agent can access)
    // ================================
    suspend fun getAllPendingBookings(): List<Booking> {
        return try {
            Log.d("BookingRepository", "=== FETCHING ALL PENDING BOOKINGS ===")
            val snapshot = bookingsCollection
                .whereEqualTo("status", "pending")
                .get()
                .await()

            Log.d("BookingRepository", "Found ${snapshot.size()} pending bookings")

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e("BookingRepository", "Error parsing booking ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error fetching pending bookings", e)
            emptyList()
        }
    }

    // ================================
    // Fetch bookings for a specific customer
    // ================================
    suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            Log.d("BookingRepository", "Fetching bookings for customer: '$userId'")

            val snapshot = bookingsCollection
                .whereEqualTo("customerId", userId)
                .get()
                .await()

            Log.d("BookingRepository", "Found ${snapshot.size()} bookings for customer")

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e("BookingRepository", "Error parsing customer booking ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error fetching customer bookings", e)
            emptyList()
        }
    }

    // ================================
    // Create a new booking (customer only)
    // ================================
    suspend fun createBooking(booking: Booking): Result<String> {
        return try {
            val currentUser = Firebase.auth.currentUser
            if (currentUser == null) {
                Log.e("BookingRepository", "No logged-in user")
                return Result.failure(Exception("User not logged in"))
            }

            val document = bookingsCollection.document()
            val bookingWithId = booking.copy(
                id = document.id,
                customerId = currentUser.uid
            )

            document.set(bookingWithId).await()
            Log.d("BookingRepository", "Booking created with ID: ${document.id}")
            Result.success(document.id)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error creating booking", e)
            Result.failure(e)
        }
    }

    // ================================
    // Confirm a booking (any agent)
    // ================================
    suspend fun confirmBooking(bookingId: String) {
        try {
            bookingsCollection.document(bookingId)
                .update("status", "confirmed")
                .await()
            Log.d("BookingRepository", "Booking $bookingId confirmed successfully")
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error confirming booking $bookingId", e)
            throw e
        }
    }

    // ================================
    // Reject a booking (any agent)
    // ================================
    suspend fun rejectBooking(bookingId: String) {
        try {
            bookingsCollection.document(bookingId)
                .update("status", "rejected")
                .await()
            Log.d("BookingRepository", "Booking $bookingId rejected successfully")
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error rejecting booking $bookingId", e)
            throw e
        }
    }

    // ================================
    // Update booking status (general)
    // ================================
    suspend fun updateBookingStatus(bookingId: String, status: String) {
        try {
            bookingsCollection.document(bookingId)
                .update("status", status)
                .await()
            Log.d("BookingRepository", "Booking $bookingId status updated to $status")
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error updating booking status", e)
            throw e
        }
    }

    // ================================
    // Get a booking by ID
    // ================================
    suspend fun getBookingById(bookingId: String): Booking? {
        return try {
            val doc = bookingsCollection.document(bookingId).get().await()
            if (doc.exists()) doc.toObject(Booking::class.java)?.copy(id = doc.id)
            else null
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error getting booking $bookingId", e)
            null
        }
    }

    // ================================
    // Debug: list all bookings
    // ================================
    suspend fun debugAllBookings() {
        try {
            val allBookings = bookingsCollection.get().await()
            Log.d("BookingRepository", "=== ALL BOOKINGS ===")
            allBookings.documents.forEach { doc ->
                val agentId = doc.getString("agentId") ?: "MISSING"
                val customerId = doc.getString("customerId") ?: "MISSING"
                val status = doc.getString("status") ?: "MISSING"
                Log.d("BookingRepository", "ID: ${doc.id}, Agent: $agentId, Customer: $customerId, Status: $status")
            }
            Log.d("BookingRepository", "=== END ALL BOOKINGS ===")
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error fetching all bookings", e)
        }
    }
}
