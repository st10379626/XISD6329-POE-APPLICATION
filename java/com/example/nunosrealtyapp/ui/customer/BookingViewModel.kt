package com.example.nunosrealtyapp.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nunosrealtyapp.data.model.Booking
import com.example.nunosrealtyapp.data.repository.BookingRepository
import com.example.nunosrealtyapp.data.repository.PropertyRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState

    private val _property = MutableStateFlow<com.example.nunosrealtyapp.data.model.Property?>(null)
    val property: StateFlow<com.example.nunosrealtyapp.data.model.Property?> = _property

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentPropertyId: String = ""

    fun setPropertyId(propertyId: String) {
        currentPropertyId = propertyId
        loadProperty()
    }

    fun loadBookings() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val userBookings = bookingRepository.getUserBookings(userId)
                    _bookings.value = userBookings
                } else {
                    _bookings.value = emptyList() // not logged in
                }
            } catch (e: Exception) {
                // Handle error (e.g., log it)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBooking(slotTime: Date, notes: String) {
        _bookingState.value = BookingState.Loading
        viewModelScope.launch {
            try {
                val customerId = FirebaseAuth.getInstance().currentUser?.uid
                if (customerId == null) {
                    _bookingState.value = BookingState.Error("User not logged in")
                    return@launch
                }

                val property = property.value
                if (property == null) {
                    _bookingState.value = BookingState.Error("Property not found")
                    return@launch
                }

                val booking = Booking(
                    id = "", // will be filled by repository
                    propertyId = currentPropertyId,
                    customerId = customerId,
                    agentId = property.createdBy,
                    slotTime = slotTime,
                    status = "pending",
                    createdAt = Date()
                )

                val result = bookingRepository.createBooking(booking)
                if (result.isSuccess) {
                    _bookingState.value = BookingState.Success
                    loadBookings() // refresh list
                } else {
                    _bookingState.value = BookingState.Error(result.exceptionOrNull()?.message ?: "Booking failed")
                }
            } catch (e: Exception) {
                _bookingState.value = BookingState.Error("Error: ${e.message}")
            }
        }
    }

    private fun loadProperty() {
        viewModelScope.launch {
            try {
                val property = propertyRepository.getPropertyById(currentPropertyId)
                _property.value = property
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    sealed class BookingState {
        object Idle : BookingState()
        object Loading : BookingState()
        object Success : BookingState()
        data class Error(val message: String) : BookingState()
    }
}
