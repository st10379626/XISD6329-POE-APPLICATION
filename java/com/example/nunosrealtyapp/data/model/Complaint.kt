package com.example.nunosrealtyapp.data.model

import java.util.Date

data class Complaint(
    val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "pending", // pending, in_progress, resolved, rejected
    val createdAt: Date = Date()
)