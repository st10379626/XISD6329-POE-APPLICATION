package com.example.nunosrealtyapp.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nunosrealtyapp.data.model.Property
import com.example.nunosrealtyapp.data.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProperties() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val allProperties = propertyRepository.getProperties() // no filters for now
                println("All properties loaded: $allProperties") // Debug log
                _properties.value = allProperties
            } catch (e: Exception) {
                println("Error loading properties: ${e.message}")
                _properties.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProperties(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _properties.value = if (query.isEmpty()) {
                    propertyRepository.getProperties()
                } else {
                    propertyRepository.searchProperties(query)
                }
            } catch (e: Exception) {
                _properties.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterProperties(isForSale: Boolean? = null, isForRent: Boolean? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _properties.value = propertyRepository.getProperties(
                    isForSale = isForSale,
                    isForRent = isForRent
                )
            } catch (e: Exception) {
                _properties.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByLocation(userLat: Double? = null, userLon: Double? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            // Pass nulls to repository if coordinates not set
            val properties = propertyRepository.getPropertiesByLocation(userLat, userLon)

            _properties.value = properties
            _isLoading.value = false
        }
    }


    // Helper function to calculate distance
    private fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0] / 1000.0 // convert meters to km
    }
}
