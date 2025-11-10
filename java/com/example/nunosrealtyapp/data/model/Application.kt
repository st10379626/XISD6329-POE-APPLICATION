package com.example.nunosrealtyapp.data.model

import java.util.Date

data class Application(
    val id: String = "",
    val propertyId: String = "",
    val propertyTitle: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val type: String = "", // "buy" or "rent"
    val status: String = "", // "pending", "approved", "rejected"
    val createdAt: Date = Date()
) {
    val formattedDate: String
        get() = android.text.format.DateFormat.getMediumDateFormat(null).format(createdAt)
}