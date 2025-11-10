package com.example.nunosrealtyapp.ui.agent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nunosrealtyapp.data.model.Booking
import com.example.nunosrealtyapp.data.repository.BookingRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _pendingBookings = MutableStateFlow<List<Booking>>(emptyList())
    val pendingBookings: StateFlow<List<Booking>> = _pendingBookings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadPendingBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bookings = bookingRepository.getAllPendingBookings() // new function
                _pendingBookings.value = bookings
                Log.d("DashboardViewModel", "Loaded ${bookings.size} pending bookings")
            } catch (e: Exception) {
                _pendingBookings.value = emptyList()
                Log.e("DashboardViewModel", "Error loading pending bookings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun acceptBooking(bookingId: String, agentId: String) {
        viewModelScope.launch {
            try {
                bookingRepository.updateBookingStatus(bookingId, "confirmed")
                loadPendingBookings()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error confirming booking", e)
            }
        }
    }

    fun rejectBooking(bookingId: String, agentId: String) {
        viewModelScope.launch {
            try {
                bookingRepository.updateBookingStatus(bookingId, "rejected")
                loadPendingBookings()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error rejecting booking", e)
            }
        }
    }

    // Fetch customer name
    suspend fun getCustomerName(customerId: String): String {
        return try {
            // Instead of fetching the name from Firestore, just return the customer ID
            customerId
        } catch (e: Exception) {
            "Unknown"
        }
    }

}
