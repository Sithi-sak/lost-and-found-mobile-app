package com.example.lostandfound.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.lostandfound.model.Chat
import com.example.lostandfound.model.ItemStatus
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.model.Message
import com.example.lostandfound.model.User
import com.example.lostandfound.utils.FirebaseStorageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Main Firebase services manager class that handles:
 * - Authentication (sign up, sign in, sign out)
 * - Firestore operations (CRUD for lost items, chats, messages)
 * - Storage operations (image uploads)
 * - User profile management
 */
class FirebaseManager {
    // Initialize Firebase components
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val lostItemsCollection = db.collection("lost_items")
    private val countersCollection = db.collection("counters")
    private val COUNTER_DOC_ID = "lost_items_counter"
    
    /**
     * Initialize FirebaseManager and verify database connection
     */
    init {
        Log.d("FirebaseManager", "Initializing FirebaseManager")
        try {
            // Verify the collections exist by performing a small query
            lostItemsCollection.limit(1).get()
                .addOnSuccessListener { 
                    Log.d("FirebaseManager", "Successfully connected to lost_items collection")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseManager", "Error accessing lost_items collection", e)
                }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error during FirebaseManager initialization", e)
        }
    }
    
    // Authentication methods
    
    /**
     * Creates a new user account and profile in Firestore
     * 
     * email User's email address
     * username User's display name
     * phoneNumber User's contact number
     * password User's account password
     * return Result containing FirebaseUser on success, Exception on failure
     */
    suspend fun signUp(email: String, username: String, phoneNumber: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d("FirebaseManager", "Starting signup process for email: $email with username: $username")
            
            // First create the authentication account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user!!
            
            Log.d("FirebaseManager", "Auth account created for uid: ${user.uid}")

            try {
                // Create the user document in Firestore with additional info
                val userDoc = db.collection("users").document(user.uid)
                val userData = mapOf(
                    "email" to email,
                    "username" to username,
                    "phoneNumber" to phoneNumber,
                    "createdAt" to System.currentTimeMillis()
                )
                
                Log.d("FirebaseManager", "Creating Firestore document for user: ${user.uid} with username: $username")
                userDoc.set(userData).await()
                Log.d("FirebaseManager", "User document created successfully with username: $username")
                
                Result.success(user)
            } catch (e: Exception) {
                // If Firestore document creation fails, delete the auth account
                Log.e("FirebaseManager", "Error creating user document, deleting auth account", e)
                user.delete().await()
                Result.failure(Exception("Failed to create user profile: ${e.message}"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error during signup", e)
            Result.failure(e)
        }
    }
    
    /**
     * Signs in an existing user with email and password
     *
     * email User's email address
     * password User's password
     * Result containing FirebaseUser on success, Exception on failure
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error signing in", e)
            Result.failure(e)
        }
    }
    
    /**
     * Signs out the current user
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Gets the currently authenticated user or null if not authenticated
     *
     * Current FirebaseUser or null
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Uploads an image to Firebase Storage from a URI
     * Checks file size limit before uploading
     *
     * imageUri URI of the image to upload
     * Result containing download URL on success, Exception on failure
     */
    suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val userId = getCurrentUser()?.uid ?: "anonymous"
            val filename = "lostitem_${UUID.randomUUID()}.jpg"
            val fileRef = storage.child("lost_item_images/$userId/$filename")
            
            // Check file size - 10MB limit
            val contentResolver = FirebaseStorageUtils.getContentResolver()
            if (contentResolver != null) {
                val fileSize = contentResolver.openFileDescriptor(imageUri, "r")?.statSize ?: 0
                val maxFileSize = 10 * 1024 * 1024 // 10MB in bytes
                
                if (fileSize > maxFileSize) {
                    return Result.failure(Exception("File size exceeds 10MB limit"))
                }
            }
            
            val uploadTask = fileRef.putFile(imageUri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error uploading image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets the next numeric ID from a counter document in Firestore
     * Creates the counter document if it doesn't exist
     *
     * Next available numeric ID
     */
    private suspend fun getNextNumericId(): Long {
        val counterDoc = countersCollection.document(COUNTER_DOC_ID)
        val counterSnapshot = counterDoc.get().await()
        
        // If counter document doesn't exist, create it with initial value
        if (!counterSnapshot.exists()) {
            counterDoc.set(mapOf("current_id" to 1L)).await()
            return 1L
        }
        
        // Increment counter atomically
        val updates = mapOf("current_id" to FieldValue.increment(1))
        counterDoc.update(updates).await()
        
        // Get the updated value
        val updatedCounterSnapshot = counterDoc.get().await()
        return updatedCounterSnapshot.getLong("current_id") ?: 1L
    }
    
    // Firestore methods
    suspend fun addLostItem(
        title: String,
        description: String,
        contact: String,
        location: String,
        imageBase64: String
    ): String {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                throw Exception("User not authenticated")
            }

            Log.d("FirebaseManager", "Adding lost item for user: ${currentUser.uid}")
            
            // Get the user's username from their profile
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            
            if (!userDoc.exists()) {
                Log.e("FirebaseManager", "addLostItem: User document does not exist")
            }
            
            val username = userDoc.getString("username")
            val userEmail = currentUser.email ?: ""
            
            Log.d("FirebaseManager", "addLostItem: Retrieved username from Firestore: $username")

            val lostItem = LostItem(
                id = "",
                title = title,
                description = description,
                contact = contact,
                location = location,
                userId = currentUser.uid,
                userEmail = userEmail,
                username = username ?: "Unknown",
                imageBase64 = imageBase64,
                timestamp = System.currentTimeMillis()
            )
            
            Log.d("FirebaseManager", "addLostItem: Created LostItem with username: ${lostItem.username}")

            val documentRef = db.collection("lost_items").document()
            val itemWithId = lostItem.copy(id = documentRef.id)
            documentRef.set(itemWithId.toMap()).await()
            
            Log.d("FirebaseManager", "addLostItem: Saved item with username: ${itemWithId.username}")
            
            documentRef.id
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Failed to add lost item: ${e.message}", e)
            throw Exception("Failed to add lost item: ${e.message}")
        }
    }
    
    suspend fun updateLostItem(itemId: String, lostItem: LostItem): Result<Unit> {
        return try {
            lostItemsCollection.document(itemId).set(lostItem).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error updating lost item", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteLostItem(itemId: String): Result<Unit> {
        return try {
            // Delete item document
            lostItemsCollection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error deleting lost item", e)
            Result.failure(e)
        }
    }
    
    fun getLostItems(): Flow<List<LostItem>> = callbackFlow {
        val listenerRegistration = lostItemsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(LostItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(items)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    fun getUserLostItems(): Flow<List<LostItem>> = callbackFlow {
        try {
            val user = getCurrentUser()
            
            Log.d("FirebaseManager", "Getting user lost items - user is ${user?.uid ?: "null"}")
            
            if (user == null) {
                Log.e("FirebaseManager", "Cannot get user lost items - user is null")
                trySend(emptyList())
                close()
                return@callbackFlow
            }
            
            val userId = user.uid
            Log.d("FirebaseManager", "Querying Firestore for user items with userId: $userId")
            
            try {
                // Option 1: Just query with filter without ordering (will work without index)
                val listenerRegistration = lostItemsCollection
                    .whereEqualTo("userId", userId)
                    // We remove the orderBy temporarily until the index is created
                    // .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FirebaseManager", "Error getting user items", error)
                            close(error)
                            return@addSnapshotListener
                        }
                        
                        Log.d("FirebaseManager", "Firestore snapshot received. Empty? ${snapshot?.isEmpty}")
                        
                        val items = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                doc.toObject(LostItem::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.e("FirebaseManager", "Error parsing document ${doc.id}", e)
                                null
                            }
                        } ?: emptyList()
                        
                        // Option 2: Sort in-memory instead of in Firestore query
                        val sortedItems = items.sortedByDescending { it.timestamp }
                        
                        Log.d("FirebaseManager", "Parsed ${sortedItems.size} user items")
                        trySend(sortedItems)
                    }
                
                awaitClose { 
                    Log.d("FirebaseManager", "Closing user items listener")
                    listenerRegistration.remove() 
                }
            } catch (e: Exception) {
                Log.e("FirebaseManager", "Error setting up Firestore listener", e)
                trySend(emptyList())
                close(e)
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Exception in getUserLostItems", e)
            trySend(emptyList())
            close(e)
        }
    }
    
    // Method to get a specific lost item by ID
    suspend fun getLostItemById(itemId: String): Result<LostItem> {
        return try {
            val documentSnapshot = lostItemsCollection.document(itemId).get().await()
            
            if (!documentSnapshot.exists()) {
                return Result.failure(Exception("Item not found"))
            }
            
            val lostItem = documentSnapshot.toObject(LostItem::class.java)
                ?.copy(id = documentSnapshot.id)
            
            if (lostItem != null) {
                Result.success(lostItem)
            } else {
                Result.failure(Exception("Failed to parse item data"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting lost item by ID", e)
            Result.failure(e)
        }
    }

    /**
     * Gets a flow of all chats for the current user
     * Updates automatically when database changes
     *
     * return Flow of Chat list that the current user participates in
     */
    fun getChats(): Flow<List<Chat>> = callbackFlow {
        try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            Log.d("FirebaseManager", "Getting chats for user: ${currentUser.uid}")
            
            val subscription = db.collection("chats")
                .whereArrayContains("participants", currentUser.uid)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FirebaseManager", "Error getting chats: ${e.message}", e)
                        if (e.message?.contains("PERMISSION_DENIED") == true) {
                            // If no chats exist yet, just emit an empty list
                            trySend(emptyList())
                        } else {
                            close(e)
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val chats = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Chat::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.e("FirebaseManager", "Error converting chat document", e)
                                null
                            }
                        }
                        Log.d("FirebaseManager", "Retrieved ${chats.size} chats")
                        trySend(chats)
                    } else {
                        Log.d("FirebaseManager", "No chats found")
                        trySend(emptyList())
                    }
                }

            awaitClose { 
                Log.d("FirebaseManager", "Closing chats listener")
                subscription.remove() 
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error in getChats", e)
            close(e)
        }
    }

    /**
     * Gets a flow of messages for a specific chat
     * Updates automatically when database changes
     *
     * param chatId ID of the chat to get messages for
     * return Flow of Message list for the specified chat
     */
    fun getChatMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        Log.d("FirebaseManager", "Starting to listen for messages in chat: $chatId")
        
        val messagesRef = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
        
        val subscription = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseManager", "Error listening for messages: ${error.message}")
                return@addSnapshotListener
            }
            
            if (snapshot == null) {
                Log.d("FirebaseManager", "No messages snapshot available")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val messages = snapshot.documents.mapNotNull { doc ->
                try {
                    val message = doc.toObject(Message::class.java)
                    if (message == null) {
                        Log.w("FirebaseManager", "Failed to convert document to Message: ${doc.id}")
                    }
                    message
                } catch (e: Exception) {
                    Log.e("FirebaseManager", "Error converting document to Message: ${e.message}")
                    null
                }
            }
            
            Log.d("FirebaseManager", "Retrieved ${messages.size} messages for chat: $chatId")
            trySend(messages)
        }
        
        awaitClose {
            Log.d("FirebaseManager", "Closing messages listener for chat: $chatId")
            subscription.remove()
        }
    }

    /**
     * Sends a new message in a specific chat
     * Also updates the chat document with the last message info
     *
     * param chatId ID of the chat to send the message in
     * param content Text content of the message
     * return Result indicating success or failure
     */
    suspend fun sendMessage(chatId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            Log.d("FirebaseManager", "Sending message in chat: $chatId")
            
            // Get username from Firestore if possible
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            val username = if (userDoc.exists()) {
                userDoc.getString("username") ?: currentUser.displayName
            } else {
                currentUser.displayName
            } ?: currentUser.email ?: "Unknown"
            
            Log.d("FirebaseManager", "Sending message with sender name: $username")
            
            val message = Message(
                chatId = chatId,
                senderId = currentUser.uid,
                senderName = username,
                content = content,
                timestamp = System.currentTimeMillis()
            )

            // Create messages subcollection if it doesn't exist
            val chatRef = db.collection("chats").document(chatId)
            val messagesRef = chatRef.collection("messages")
            
            // Add the message
            val messageRef = messagesRef.add(message).await()
            Log.d("FirebaseManager", "Message sent with ID: ${messageRef.id}")

            // Update chat document with last message info
            chatRef.update(
                mapOf(
                    "lastMessage" to content,
                    "lastMessageTimestamp" to message.timestamp
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error sending message", e)
            Result.failure(e)
        }
    }

    /**
     * Creates a new chat or returns existing chat between users about a specific item
     *
     * param otherUserId ID of the other user to chat with
     * param itemId ID of the lost item the chat is about
     * return Result containing chat ID on success, Exception on failure
     */
    suspend fun createOrOpenChat(otherUserId: String, itemId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            Log.d("FirebaseManager", "Creating/opening chat with user: $otherUserId for item: $itemId")

            // Get the lost item first to get user information
            val lostItem = lostItemsCollection.document(itemId).get().await()
                .toObject(LostItem::class.java) ?: throw Exception("Item not found")

            // Check if chat already exists
            val existingChat = db.collection("chats")
                .whereArrayContains("participants", currentUser.uid)
                .get()
                .await()
                .documents
                .firstOrNull { doc ->
                    val participants = doc.get("participants") as? List<*>
                    val chatItemId = doc.getString("itemId")
                    participants?.containsAll(listOf(currentUser.uid, otherUserId)) == true &&
                    chatItemId == itemId
                }

            if (existingChat != null) {
                Log.d("FirebaseManager", "Found existing chat: ${existingChat.id}")
                return@withContext Result.success(existingChat.id)
            }

            // Create new chat
            val chat = hashMapOf(
                "participants" to listOf(currentUser.uid, otherUserId),
                "itemId" to itemId,
                "lastMessage" to "",
                "lastMessageTimestamp" to System.currentTimeMillis(),
                "createdAt" to System.currentTimeMillis(),
                "otherUserName" to lostItem.username,
                "otherUserEmail" to lostItem.userEmail
            )

            val chatRef = db.collection("chats").add(chat).await()
            Log.d("FirebaseManager", "Created new chat: ${chatRef.id}")

            Result.success(chatRef.id)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error creating/opening chat", e)
            Result.failure(e)
        }
    }

    /**
     * Converts an image InputStream to Base64 string
     * Resizes the image to reduce size while maintaining aspect ratio
     *
     * inputStream InputStream of the image to convert
     * return Result containing Base64 string on success, Exception on failure
     */
    suspend fun convertImageToBase64(inputStream: InputStream): Result<String> {
        return try {
            // Read the input stream into a bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: return Result.failure(Exception("Failed to decode image"))

            // Calculate new dimensions while maintaining aspect ratio
            val maxDimension = 1024
            val ratio = minOf(
                maxDimension.toFloat() / originalBitmap.width,
                maxDimension.toFloat() / originalBitmap.height
            )
            val newWidth = (originalBitmap.width * ratio).toInt()
            val newHeight = (originalBitmap.height * ratio).toInt()

            // Create scaled bitmap
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )

            // Convert to Base64
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()
            val base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            // Clean up
            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()
            outputStream.close()

            Result.success(base64String)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error converting image to Base64", e)
            Result.failure(e)
        }
    }

    /**
     * Gets user data for a specific user ID
     *
     * userId ID of the user to get data for
     * return Result containing User object on success, Exception on failure
     */
    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to parse user data"))
                }
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting user data", e)
            Result.failure(e)
        }
    }

    /**
     * Gets the phone number of the current user
     *
     * Phone number string or empty string if not found
     */
    suspend fun getCurrentUserPhone(): String {
        val currentUser = getCurrentUser() ?: return ""
        return try {
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            userDoc.getString("phoneNumber") ?: ""
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting user phone number", e)
            ""
        }
    }

    /**
     * Gets the username of the current user
     *
     * Username string or empty string if not found
     */
    suspend fun getCurrentUsername(): String {
        val currentUser = getCurrentUser()
        
        if (currentUser == null) {
            Log.e("FirebaseManager", "getCurrentUsername: Current user is null")
            return ""
        }
        
        Log.d("FirebaseManager", "getCurrentUsername: Getting username for user ${currentUser.uid}")
        
        return try {
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            
            if (!userDoc.exists()) {
                Log.e("FirebaseManager", "getCurrentUsername: User document does not exist")
                return ""
            }
            
            val username = userDoc.getString("username")
            Log.d("FirebaseManager", "getCurrentUsername: Username from Firestore: $username")
            
            username ?: ""
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting username", e)
            ""
        }
    }
    
    /**
     * Gets complete user data for the current user
     *
     * @return Result containing User object on success, Exception on failure
     */
    suspend fun getCurrentUserData(): Result<User> {
        val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not authenticated"))
        return getUserData(currentUser.uid)
    }
    
    /**
     * Updates the current user's profile information
     *
     * @param username New username to set
     * @param phoneNumber New phone number to set
     * @return Result indicating success or failure
     */
    suspend fun updateUserProfile(username: String, phoneNumber: String): Result<Unit> {
        val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val userDoc = db.collection("users").document(currentUser.uid)
            userDoc.update(
                mapOf(
                    "username" to username,
                    "phoneNumber" to phoneNumber
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error updating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Gets a paginated list of lost items
     *
     * startIndex Index to start from
     * pageSize Number of items to fetch
     * List of LostItem objects for the requested page
     */
    suspend fun getLostItems(startIndex: Int, pageSize: Int): List<LostItem> {
        return try {
            val snapshot = db.collection("lost_items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            // Get all items and then manually paginate
            val allItems = snapshot.documents.mapNotNull { doc ->
                doc.toObject(LostItem::class.java)?.copy(id = doc.id)
            }

            // Return the requested page of items
            allItems.drop(startIndex).take(pageSize)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting lost items", e)
            emptyList()
        }
    }

    /**
     * Gets the total count of lost items in the database
     *
     * Total number of items
     */
    suspend fun getTotalItemCount(): Int {
        return try {
            val snapshot = db.collection("lost_items")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting total item count", e)
            0
        }
    }

    /**
     * Updates the status of a lost item (LOST or FOUND)
     *
     * itemId ID of the item to update
     * newStatus New status to set
     * Exception if update fails
     */
    suspend fun updateItemStatus(itemId: String, newStatus: ItemStatus) {
        try {
            db.collection("lost_items")
                .document(itemId)
                .update("status", newStatus.name)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error updating item status", e)
            throw e
        }
    }
} 