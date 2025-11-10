package com.example.nunosrealtyapp.ui.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nunosrealtyapp.data.model.Property
import com.example.nunosrealtyapp.data.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddListingViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun createListing(
        title: String,
        description: String,
        price: Double,
        isForSale: Boolean,
        isForRent: Boolean,
        city: String,
        province: String,
        address: String,
        latitude: Double,
        longitude: Double,
        beds: Int,
        baths: Int,
        area: Int,
        createdBy: String,
        images: List<android.net.Uri>
    ) {
        _uploadState.value = UploadState.Loading

        viewModelScope.launch {
            try {
                // First upload all images
                val imageUrls = mutableListOf<String>()
                for (imageUri in images) {
                    val result = propertyRepository.uploadPropertyImage(imageUri)
                    if (result.isSuccess) {
                        imageUrls.add(result.getOrThrow())
                    } else {
                        _uploadState.value = UploadState.Error("Failed to upload images: ${result.exceptionOrNull()?.message}")
                        return@launch
                    }
                }

                // Create property object
                val property = Property(
                    title = title,
                    description = description,
                    price = price,
                    isForSale = isForSale,
                    isForRent = isForRent,
                    city = city,
                    province = province,
                    country = "South Africa",
                    address = address,
                    latitude = latitude,
                    longitude = longitude,
                    beds = beds,
                    baths = baths,
                    areaSqft = area,
                    rating = 0.0,
                    images = imageUrls,
                    createdBy = createdBy, // You'll need to get the current user ID
                    createdAt = Date(),
                    status = "active"
                )

                // Save property to Firestore
                val result = propertyRepository.addProperty(property)
                if (result.isSuccess) {
                    _uploadState.value = UploadState.Success
                } else {
                    _uploadState.value = UploadState.Error("Failed to create listing: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Error: ${e.message}")
            }
        }
    }

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        object Success : UploadState()
        data class Error(val message: String) : UploadState()
    }
}