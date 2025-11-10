package com.example.nunosrealtyapp.data.seed

import android.content.Context
import com.example.nunosrealtyapp.data.model.Property
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

object Seeder {

    fun seedProperties(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()

            // Check if properties already exist
            val snapshot = db.collection("properties").limit(1).get().await()
            if (!snapshot.isEmpty) return@launch

            // Use images already uploaded in Firebase Storage
            val image1Url = "https://images.pexels.com/photos/462358/pexels-photo-462358.jpeg?cs=srgb&dl=architectural-design-architecture-blue-sky-462358.jpg&fm=jpg"
            val image2Url = "https://images.pexels.com/photos/462358/pexels-photo-462358.jpeg?cs=srgb&dl=architectural-design-architecture-blue-sky-462358.jpg&fm=jpg"
            val image3Url = "https://images.pexels.com/photos/462358/pexels-photo-462358.jpeg?cs=srgb&dl=architectural-design-architecture-blue-sky-462358.jpg&fm=jpg"

            // Define properties
            val properties = listOf(
                Property(
                    id = UUID.randomUUID().toString(),
                    title = "Luxury Apartment in Sandton",
                    city = "Sandton",
                    province = "Gauteng",
                    beds = 3,
                    baths = 2,
                    price = 1200000.0,
                    isForSale = true,
                    isForRent = false,
                    latitude = -26.1076,
                    longitude = 28.0567,
                    createdBy = "agentUid1",
                    createdAt = com.google.firebase.Timestamp.now().toDate(),
                    images = listOf(image1Url),
                    status = "active"
                ),
                Property(
                    id = UUID.randomUUID().toString(),
                    title = "Cozy Flat in Cape Town",
                    city = "Cape Town",
                    province = "Western Cape",
                    beds = 2,
                    baths = 1,
                    price = 800000.0,
                    isForSale = true,
                    isForRent = false,
                    latitude = -33.9249,
                    longitude = 18.4241,
                    createdBy = "agentUid2",
                    createdAt = com.google.firebase.Timestamp.now().toDate(),
                    images = listOf(image2Url),
                    status = "active"
                ),
                Property(
                    id = UUID.randomUUID().toString(),
                    title = "Modern House in Pretoria",
                    city = "Pretoria",
                    province = "Gauteng",
                    beds = 4,
                    baths = 3,
                    price = 1500000.0,
                    isForSale = true,
                    isForRent = false,
                    latitude = -25.7479,
                    longitude = 28.2293,
                    createdBy = "agentUid3",
                    createdAt = com.google.firebase.Timestamp.now().toDate(),
                    images = listOf(image3Url),
                    status = "active"
                )
            )

            // Save to Firestore
            properties.forEach { property ->
                db.collection("properties").document(property.id)
                    .set(property)
            }
        }
    }
}
