package com.example.nunosrealtyapp.ui.shared

import android.location.Location
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
class MapViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    private val _selectedLocation = MutableStateFlow("Sandton, Gauteng")
    val selectedLocation: StateFlow<String> = _selectedLocation

    fun loadProperties() {
        viewModelScope.launch {
            try {
                val allProperties = propertyRepository.getProperties()
                _properties.value = allProperties
            } catch (e: Exception) {
                _properties.value = emptyList()
            }
        }
    }

    fun searchLocation(query: String) {
        viewModelScope.launch {
            try {
                _properties.value = if (query.isEmpty()) {
                    propertyRepository.getProperties()
                } else {
                    propertyRepository.searchProperties(query)
                }
                _selectedLocation.value = query
            } catch (e: Exception) {
                _properties.value = emptyList()
            }
        }
    }

    fun setCurrentLocation(location: Location) {
        _currentLocation.value = location
        _selectedLocation.value = "Current Location"
        getNearbyProperties(location.latitude, location.longitude)
    }

    fun getNearbyProperties(userLat: Double, userLng: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            try {
                val allProperties = propertyRepository.getProperties()
                val nearby = allProperties.filter { property ->
                    distanceBetween(userLat, userLng, property.latitude, property.longitude) <= radiusKm
                }
                _properties.value = nearby
            } catch (e: Exception) {
                _properties.value = emptyList()
            }
        }
    }

    fun getProperty(propertyId: String): Property? {
        return _properties.value.find { it.id == propertyId }
    }

    private fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0] / 1000.0 // return in KM
    }
}
