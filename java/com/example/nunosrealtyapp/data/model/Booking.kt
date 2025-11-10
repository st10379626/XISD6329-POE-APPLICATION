package com.example.nunosrealtyapp.data.model


import java.util.Date

data class Booking(
    val id: String = "",
    val propertyId: String = "",
    val customerId: String = "",
    val agentId: String = "",
    val slotTime: Date = Date(),
    val status: String = "pending", // pending, confirmed, cancelled
    val createdAt: Date = Date()
)