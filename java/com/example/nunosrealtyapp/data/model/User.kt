package com.example.nunosrealtyapp.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "customer",      // "agent" or "customer"
    val companyName: String? = null,     // Optional, only for agents
    val companyDocUrl: String? = null,
    val profileImageUrl: String? = null,// Optional, only for agents
    @ServerTimestamp
    val createdAt: Date? = null
)
