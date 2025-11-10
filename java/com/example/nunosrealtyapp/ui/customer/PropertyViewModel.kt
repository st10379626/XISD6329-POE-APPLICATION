package com.example.nunosrealtyapp.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nunosrealtyapp.data.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _property = MutableStateFlow<com.example.nunosrealtyapp.data.model.Property?>(null)
    val property: StateFlow<com.example.nunosrealtyapp.data.model.Property?> = _property

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProperty(propertyId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val property = propertyRepository.getPropertyById(propertyId)
                _property.value = property
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}