package com.example.lostandfound.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.firebase.FirebaseManager
import com.example.lostandfound.model.LostItem
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class LostAndFoundViewModel : ViewModel() {
    
    private val firebaseManager = FirebaseManager()
    
    // Auth state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Form state
    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState.asStateFlow()
    
    // Detail state for individual item
    private val _detailState = MutableStateFlow<DetailState>(DetailState.Idle)
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()
    
    // Image upload state
    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        val currentUser = firebaseManager.getCurrentUser()
        _authState.value = if (currentUser != null) {
            AuthState.Authenticated(currentUser)
        } else {
            AuthState.Unauthenticated
        }
    }
    
    fun fetchLostItemById(itemId: String) {
        _detailState.value = DetailState.Loading
        viewModelScope.launch {
            val result = firebaseManager.getLostItemById(itemId)
            result.fold(
                onSuccess = { item ->
                    _detailState.value = DetailState.Success(item)
                },
                onFailure = { exception ->
                    _detailState.value = DetailState.Error(exception.message ?: "Failed to load item details")
                }
            )
        }
    }
    
    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseManager.signIn(email, password)
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Authentication failed")
                }
            )
        }
    }
    
    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseManager.signUp(email, password)
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
            )
        }
    }
    
    fun signOut() {
        firebaseManager.signOut()
        _authState.value = AuthState.Unauthenticated
    }
    
    fun uploadImage(imageUri: Uri) {
        _imageUploadState.value = ImageUploadState.Loading
        viewModelScope.launch {
            val result = firebaseManager.uploadImage(imageUri)
            result.fold(
                onSuccess = { url ->
                    _imageUploadState.value = ImageUploadState.Success(url)
                },
                onFailure = { exception ->
                    _imageUploadState.value = ImageUploadState.Error(exception.message ?: "Failed to upload image")
                }
            )
        }
    }
    
    fun createLostItem(title: String, description: String, contact: String, imageUrl: String = "") {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val lostItem = LostItem(
                title = title,
                description = description,
                contact = contact,
                timestamp = System.currentTimeMillis(),
                imageUrl = imageUrl
            )
            
            val result = firebaseManager.addLostItem(lostItem)
            result.fold(
                onSuccess = {
                    _formState.value = FormState.Success("Item posted successfully")
                },
                onFailure = { exception ->
                    _formState.value = FormState.Error(exception.message ?: "Failed to post item")
                }
            )
        }
    }
    
    fun deleteLostItem(itemId: String) {
        viewModelScope.launch {
            firebaseManager.deleteLostItem(itemId)
        }
    }
    
    fun getAllLostItems(): Flow<List<LostItem>> {
        return firebaseManager.getLostItems()
    }
    
    fun getUserLostItems(): Flow<List<LostItem>> {
        return try {
            // Verify we have a valid user before attempting to get items
            if (authState.value !is AuthState.Authenticated) {
                Log.w("LostAndFoundViewModel", "Attempted to get user items while not authenticated")
                flowOf(listOf())
            } else {
                firebaseManager.getUserLostItems()
                    .catch { e ->
                        Log.e("LostAndFoundViewModel", "Error in user items flow", e)
                        emit(listOf())
                    }
            }
        } catch (e: Exception) {
            Log.e("LostAndFoundViewModel", "Exception getting user items", e)
            flowOf(listOf())
        }
    }
    
    fun resetFormState() {
        _formState.value = FormState.Idle
    }
    
    fun resetImageUploadState() {
        _imageUploadState.value = ImageUploadState.Idle
    }
    
    fun resetDetailState() {
        _detailState.value = DetailState.Idle
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    data class Success(val message: String) : FormState()
    data class Error(val message: String) : FormState()
}

sealed class DetailState {
    object Idle : DetailState()
    object Loading : DetailState()
    data class Success(val item: LostItem) : DetailState()
    data class Error(val message: String) : DetailState()
}

sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Loading : ImageUploadState()
    data class Success(val imageUrl: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
} 