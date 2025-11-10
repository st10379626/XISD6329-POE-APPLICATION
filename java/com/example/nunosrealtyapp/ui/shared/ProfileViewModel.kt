package com.example.nunosrealtyapp.ui.shared

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nunosrealtyapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<com.example.nunosrealtyapp.data.model.User?>(null)
    val userProfile: StateFlow<com.example.nunosrealtyapp.data.model.User?> = _userProfile

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // Get current user from repository
                val user = userRepository.getCurrentUser()
                _userProfile.value = user
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateProfile(fullName: String, companyName: String?, profileImageUri: Uri?) {
        _updateState.value = UpdateState.Loading
        viewModelScope.launch {
            try {
                val result = userRepository.updateUserProfile(fullName, companyName, profileImageUri)
                if (result.isSuccess) {
                    _updateState.value = UpdateState.Success
                    // Reload user profile
                    loadUserProfile()
                } else {
                    _updateState.value = UpdateState.Error(result.exceptionOrNull()?.message ?: "Update failed")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Error: ${e.message}")
            }
        }
    }

    fun logout() {
        userRepository.logout()
    }

    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
}