package com.example.lostandfound.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.firebase.FirebaseManager
import com.example.lostandfound.model.Chat
import com.example.lostandfound.model.ItemStatus
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.model.Message
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream

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
    
    // Chat-related state
    private val _chatState = MutableStateFlow<ChatState>(ChatState.Loading)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _postState = MutableStateFlow<PostState>(PostState.Initial)
    val postState: StateFlow<PostState> = _postState.asStateFlow()

    private val _imageState = MutableStateFlow<ImageState>(ImageState.NoImage)
    val imageState: StateFlow<ImageState> = _imageState.asStateFlow()
    
    private val _items = MutableStateFlow<List<LostItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems = _hasMoreItems.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages = _totalPages.asStateFlow()

    private val pageSize = 10
    private var totalItems = 0
    
    init {
        checkAuthState()
        viewModelScope.launch {
            try {
                totalItems = firebaseManager.getTotalItemCount()
                _totalPages.value = (totalItems + pageSize - 1) / pageSize
            } catch (e: Exception) {
                Log.e("ViewModel", "Error getting total item count", e)
            }
        }
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
    
    fun signUp(email: String, username: String, phoneNumber: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseManager.signUp(email, username, phoneNumber, password)
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
    
    fun createLostItem(
        title: String,
        description: String,
        contact: String,
        location: String,
        imageBase64: String
    ) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            try {
                val itemId = firebaseManager.addLostItem(
                    title = title,
                    description = description,
                    contact = contact,
                    location = location,
                    imageBase64 = imageBase64
                )
                _postState.value = PostState.Success(itemId)
            } catch (e: Exception) {
                _postState.value = PostState.Error(e.message ?: "Unknown error occurred")
            }
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

    fun getChats() {
        viewModelScope.launch {
            try {
                firebaseManager.getChats()
                    .catch { e ->
                        _chatState.value = ChatState.Error(e.message ?: "Error fetching chats")
                    }
                    .collect { chatsList ->
                        _chats.value = chatsList
                        _chatState.value = ChatState.Success
                    }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Error fetching chats")
            }
        }
    }

    fun getChatMessages(chatId: String) {
        viewModelScope.launch {
            try {
                firebaseManager.getChatMessages(chatId)
                    .catch { e ->
                        _chatState.value = ChatState.Error(e.message ?: "Error fetching messages")
                    }
                    .collect { messagesList ->
                        _messages.value = messagesList
                    }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Error fetching messages")
            }
        }
    }

    fun sendMessage(chatId: String, content: String) {
        viewModelScope.launch {
            try {
                val result = firebaseManager.sendMessage(chatId, content)
                result.onFailure { e ->
                    _chatState.value = ChatState.Error(e.message ?: "Error sending message")
                }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Error sending message")
            }
        }
    }

    fun createOrOpenChat(otherUserId: String, itemId: String, onChatCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = firebaseManager.createOrOpenChat(otherUserId, itemId)
                result.fold(
                    onSuccess = { chatId ->
                        onChatCreated(chatId)
                    },
                    onFailure = { e ->
                        _chatState.value = ChatState.Error(e.message ?: "Error creating chat")
                    }
                )
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Error creating chat")
            }
        }
    }

    fun handleImageSelection(inputStream: InputStream) {
        viewModelScope.launch {
            _imageState.value = ImageState.Loading
            try {
                val result = firebaseManager.convertImageToBase64(inputStream)
                result.fold(
                    onSuccess = { base64String ->
                        _imageState.value = ImageState.Success(base64String)
                    },
                    onFailure = { e ->
                        _imageState.value = ImageState.Error(e.message ?: "Error processing image")
                    }
                )
            } catch (e: Exception) {
                _imageState.value = ImageState.Error(e.message ?: "Error processing image")
            }
        }
    }

    fun resetImageState() {
        _imageState.value = ImageState.NoImage
    }

    fun resetPostState() {
        _postState.value = PostState.Initial
    }

    fun loadMoreItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // First get total count to update pages
                totalItems = firebaseManager.getTotalItemCount()
                _totalPages.value = (totalItems + pageSize - 1) / pageSize

                // Then load the current page
                val startIndex = (_currentPage.value - 1) * pageSize
                val items = firebaseManager.getLostItems(startIndex, pageSize)
                _items.value = items
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading items", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun nextPage() {
        if (_currentPage.value < _totalPages.value) {
            _currentPage.value += 1
            loadMoreItems()
        }
    }

    fun previousPage() {
        if (_currentPage.value > 1) {
            _currentPage.value -= 1
            loadMoreItems()
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPage.value = 1
            _items.value = emptyList()
            try {
                totalItems = firebaseManager.getTotalItemCount()
                _totalPages.value = (totalItems + pageSize - 1) / pageSize
                loadMoreItems()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error refreshing items", e)
                _isLoading.value = false
            }
        }
    }

    fun updateItemStatus(itemId: String, newStatus: ItemStatus) {
        viewModelScope.launch {
            try {
                firebaseManager.updateItemStatus(itemId, newStatus)
                // Update local state
                _items.update { currentItems ->
                    currentItems.map { item ->
                        if (item.id == itemId) item.copy(status = newStatus) else item
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error updating item status", e)
            }
        }
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
    data class Success(val imageBase64: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}

sealed class ChatState {
    object Loading : ChatState()
    object Success : ChatState()
    data class Error(val message: String) : ChatState()
}

sealed class ImageState {
    object NoImage : ImageState()
    object Loading : ImageState()
    data class Success(val base64String: String) : ImageState()
    data class Error(val message: String) : ImageState()
}

sealed class PostState {
    data object Initial : PostState()
    data object Loading : PostState()
    data class Success(val id: String) : PostState()
    data class Error(val message: String) : PostState()
} 