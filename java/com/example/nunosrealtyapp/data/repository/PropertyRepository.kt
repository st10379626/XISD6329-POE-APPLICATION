package com.example.nunosrealtyapp.data.repository

import android.net.Uri
import com.example.nunosrealtyapp.data.model.Property
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun getProperties(
        isForSale: Boolean? = null,
        isForRent: Boolean? = null,
        city: String? = null,
        limit: Int = 20
    ): List<Property> {
        var query = db.collection("properties")
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        if (isForSale != null) query = query.whereEqualTo("isForSale", isForSale)
        if (isForRent != null) query = query.whereEqualTo("isForRent", isForRent)
        if (!city.isNullOrEmpty()) query = query.whereEqualTo("city", city)


        val snapshot = query.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Property::class.java) }
    }

    suspend fun getPropertyById(id: String): Property? {
        val document = db.collection("properties").document(id).get().await()
        return document.toObject(Property::class.java)
    }

    suspend fun addProperty(property: Property): Result<String> {
        return try {
            val document = db.collection("properties").document()
            val propertyWithId = property.copy(id = document.id)
            document.set(propertyWithId).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProperty(property: Property): Result<Unit> {
        return try {
            db.collection("properties").document(property.id).set(property).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAgentProperties(agentId: String): List<Property> {
        val snapshot = db.collection("properties")
            .whereEqualTo("createdBy", agentId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(Property::class.java) }
    }


    suspend fun uploadPropertyImage(imageUri: Uri): Result<String> {
        return try {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("property_images/$fileName")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProperties(query: String): List<Property> {
        val snapshot = db.collection("properties")
            .whereEqualTo("status", "active")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .get().await()

        return snapshot.documents.mapNotNull { it.toObject(Property::class.java) }
    }

    suspend fun getPropertiesByLocation(
        latitude: Double?,
        longitude: Double?,
        radiusKm: Double = 50.0 // default 50 km radius
    ): List<Property> {
        val allProperties = getProperties(limit = 100)

        // If no coordinates provided, return all active properties
        if (latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0)) {
            return allProperties
        }

        return allProperties.filter { property ->
            val distance =
                calculateDistance(latitude, longitude, property.latitude, property.longitude)
            distance <= radiusKm
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2.0)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun Double.pow(power: Double) = Math.pow(this, power)


}
