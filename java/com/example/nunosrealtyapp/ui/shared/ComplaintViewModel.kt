package com.example.nunosrealtyapp.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nunosrealtyapp.data.repository.ComplaintRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplaintViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _complaints = MutableStateFlow<List<com.example.nunosrealtyapp.data.model.Complaint>>(emptyList())
    val complaints: StateFlow<List<com.example.nunosrealtyapp.data.model.Complaint>> = _complaints

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadComplaints() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userComplaints = complaintRepository.getUserComplaints(userId)
                _complaints.value = userComplaints
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitComplaint(subject: String, message: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = complaintRepository.submitComplaint(userId, subject, message)
                if (result.isSuccess) {
                    loadComplaints()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

}