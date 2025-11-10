package com.example.nunosrealtyapp.ui.agent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nunosrealtyapp.data.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _listings = MutableStateFlow<List<com.example.nunosrealtyapp.data.model.Property>>(emptyList())
    val listings: StateFlow<List<com.example.nunosrealtyapp.data.model.Property>> = _listings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadListings() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val currentUserId = currentUser?.uid

                Log.d("ListingsDebug", "Current logged-in UID: $currentUserId")
                if (currentUserId == null) {
                    Log.d("ListingsDebug", "No user is logged in!")
                    _listings.value = emptyList()
                    return@launch
                }

                // Fetch all properties for this agent
                val agentListings = propertyRepository.getAgentProperties(currentUserId)
                Log.d("ListingsDebug", "Properties found for this agent: ${agentListings.size}")
                agentListings.forEach { property ->
                    Log.d("ListingsDebug", "Property: ${property.title}, createdBy: ${property.createdBy}")
                }

                _listings.value = agentListings

            } catch (e: Exception) {
                Log.e("ListingsDebug", "Error fetching listings: ${e.message}")
                _listings.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }



    suspend fun deleteListing(listingId: String): Boolean {
        return try {
            // Implement delete functionality
            true
        } catch (e: Exception) {
            false
        }
    }
}