package com.example.nunosrealtyapp.data.model


import java.util.Date

data class Property(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val isForSale: Boolean = false,
    val isForRent: Boolean = false,
    val city: String = "",
    val province: String = "",
    val country: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val beds: Int = 0,
    val baths: Int = 0,
    val areaSqft: Int = 0,
    val rating: Double = 0.0,
    val images: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Date = Date(),
    val status: String = "active" // active, archived
)