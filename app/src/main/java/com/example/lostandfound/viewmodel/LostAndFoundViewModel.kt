package com.example.lostandfound.viewmodel

// Import necessary Android and Firebase components for ViewModel functionality
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

/**
 * Main ViewModel class that manages the application's data and business logic.
 * Handles authentication, item management, chat functionality, and user preferences.
 */
class LostAndFoundViewModel : ViewModel() {
    
    // Firebase manager instance for handling all Firebase operations
    private val firebaseManager = FirebaseManager()
    
    // Authentication state management
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // User profile information states
    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()
    
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    // Theme preference state
    private val _themeState = MutableStateFlow<ThemeState>(ThemeState.LIGHT)
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()
    
    // Profile update state
    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState: StateFlow<ProfileUpdateState> = _profileUpdateState.asStateFlow()
    
    // Form submission state
    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState.asStateFlow()
    
    // Item detail view state
    private val _detailState = MutableStateFlow<DetailState>(DetailState.Idle)
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()
    
    // Image upload state
    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()
    
    // Chat functionality states
    private val _chatState = MutableStateFlow<ChatState>(ChatState.Loading)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    // Post creation state
    private val _postState = MutableStateFlow<PostState>(PostState.Initial)
    val postState: StateFlow<PostState> = _postState.asStateFlow()

    // Image selection state
    private val _imageState = MutableStateFlow<ImageState>(ImageState.NoImage)
    val imageState: StateFlow<ImageState> = _imageState.asStateFlow()
    
    // Lost items list and pagination states
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

    // Pagination configuration
    private val pageSize = 5
    private var totalItems = 0
    
    // Initialize ViewModel with necessary data
    init {
        checkAuthState()
        viewModelScope.launch {
            try {
                totalItems = firebaseManager.getTotalItemCount()
                _totalPages.value = (totalItems + pageSize - 1) / pageSize
                fetchUserPhone()
                fetchUserName()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error getting total item count", e)
            }
        }
    }
    
    // Fetch user's phone number from Firebase
    private fun fetchUserPhone() {
        viewModelScope.launch {
            try {
                _userPhone.value = firebaseManager.getCurrentUserPhone()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching user phone", e)
            }
        }
    }
    
    // Fetch user's name from Firebase
    private fun fetchUserName() {
        viewModelScope.launch {
            try {
                _userName.value = firebaseManager.getCurrentUsername()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching username", e)
            }
        }
    }
    
    // Check and update authentication state
    private fun checkAuthState() {
        val currentUser = firebaseManager.getCurrentUser()
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
            fetchUserPhone()
            fetchUserName()
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    // Fetch a specific lost item by ID
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
    
    // Handle user sign in
    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseManager.signIn(email, password)
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                    // Fetch user data after successful authentication
                    fetchUserPhone()
                    fetchUserName()
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Authentication failed")
                }
            )
        }
    }
    
    // Handle user registration
    fun signUp(email: String, username: String, phoneNumber: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = firebaseManager.signUp(email, username, phoneNumber, password)
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                    // Set username and phone directly since we know them
                    _userName.value = username
                    _userPhone.value = phoneNumber
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
            )
        }
    }
    
    // Handle user sign out
    fun signOut() {
        firebaseManager.signOut()
        _authState.value = AuthState.Unauthenticated
        // Clear user data
        _userName.value = ""
        _userPhone.value = ""
    }
    
    // Upload image to Firebase Storage
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
    
    // Create a new lost item
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
    
    // Delete a lost item
    fun deleteLostItem(itemId: String) {
        viewModelScope.launch {
            firebaseManager.deleteLostItem(itemId)
        }
    }
    
    // Get all lost items
    fun getAllLostItems(): Flow<List<LostItem>> {
        return firebaseManager.getLostItems()
    }
    
    // Get lost items posted by the current user
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
    
    // Reset form state
    fun resetFormState() {
        _formState.value = FormState.Idle
    }
    
    // Reset image upload state
    fun resetImageUploadState() {
        _imageUploadState.value = ImageUploadState.Idle
    }
    
    // Reset detail view state
    fun resetDetailState() {
        _detailState.value = DetailState.Idle
    }

    // Get all chats for the current user
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

    // Get messages for a specific chat
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

    // Send a message in a chat
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

    // Create a new chat or open an existing one
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

    // Handle image selection and conversion to Base64
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

    // Reset image state
    fun resetImageState() {
        _imageState.value = ImageState.NoImage
    }

    // Reset post creation state
    fun resetPostState() {
        _postState.value = PostState.Initial
    }

    // Load more items for pagination
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

    // Navigate to next page
    fun nextPage() {
        if (_currentPage.value < _totalPages.value) {
            _currentPage.value += 1
            loadMoreItems()
        }
    }

    // Navigate to previous page
    fun previousPage() {
        if (_currentPage.value > 1) {
            _currentPage.value -= 1
            loadMoreItems()
        }
    }

    // Refresh items list
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

    // Update item status (e.g., found/lost)
    fun updateItemStatus(itemId: String, newStatus: ItemStatus) {
        viewModelScope.launch {
            // Get the current item
            val result = firebaseManager.getLostItemById(itemId)
            result.fold(
                onSuccess = { item ->
                    // Update the item with new status
                    val updatedItem = item.copy(status = newStatus)
                    firebaseManager.updateLostItem(itemId, updatedItem).fold(
                        onSuccess = {
                            // Update detail state if we're in detail view
                            if (_detailState.value is DetailState.Success) {
                                _detailState.value = DetailState.Success(updatedItem)
                            }
                            // Update items list if we're in list view
                            _items.update { currentItems ->
                                currentItems.map { 
                                    if (it.id == itemId) updatedItem else it 
                                }
                            }
                        },
                        onFailure = { exception ->
                            _detailState.value = DetailState.Error(exception.message ?: "Failed to update status")
                        }
                    )
                },
                onFailure = { exception ->
                    _detailState.value = DetailState.Error(exception.message ?: "Failed to find item")
                }
            )
        }
    }

    // Update user profile information
    fun updateUserProfile(username: String, phoneNumber: String) {
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading
            
            try {
                val result = firebaseManager.updateUserProfile(username, phoneNumber)
                result.fold(
                    onSuccess = {
                        _userName.value = username
                        _userPhone.value = phoneNumber
                        _profileUpdateState.value = ProfileUpdateState.Success
                    },
                    onFailure = { e ->
                        _profileUpdateState.value = ProfileUpdateState.Error(e.message ?: "Failed to update profile")
                    }
                )
            } catch (e: Exception) {
                _profileUpdateState.value = ProfileUpdateState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
    
    // Reset profile update state
    fun resetProfileUpdateState() {
        _profileUpdateState.value = ProfileUpdateState.Idle
    }

    // Toggle between light and dark theme
    fun toggleTheme() {
        _themeState.value = if (_themeState.value == ThemeState.LIGHT) ThemeState.DARK else ThemeState.LIGHT
    }
}

// Authentication state sealed class
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Form submission state sealed class
sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    data class Success(val message: String) : FormState()
    data class Error(val message: String) : FormState()
}

// Item detail view state sealed class
sealed class DetailState {
    object Idle : DetailState()
    object Loading : DetailState()
    data class Success(val item: LostItem) : DetailState()
    data class Error(val message: String) : DetailState()
}

// Image upload state sealed class
sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Loading : ImageUploadState()
    data class Success(val imageBase64: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}

// Chat state sealed class
sealed class ChatState {
    object Loading : ChatState()
    object Success : ChatState()
    data class Error(val message: String) : ChatState()
}

// Image selection state sealed class
sealed class ImageState {
    object NoImage : ImageState()
    object Loading : ImageState()
    data class Success(val base64String: String) : ImageState()
    data class Error(val message: String) : ImageState()
}

// Post creation state sealed class
sealed class PostState {
    data object Initial : PostState()
    data object Loading : PostState()
    data class Success(val id: String) : PostState()
    data class Error(val message: String) : PostState()
}

// Profile update state sealed class
sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    object Success : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}

// Theme state sealed class
sealed class ThemeState {
    object LIGHT : ThemeState()
    object DARK : ThemeState()
} 