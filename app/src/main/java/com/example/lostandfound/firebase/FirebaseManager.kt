package com.example.lostandfound.firebase

import android.net.Uri
import android.util.Log
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.utils.FirebaseStorageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val lostItemsCollection = db.collection("lost_items")
    private val countersCollection = db.collection("counters")
    private val COUNTER_DOC_ID = "lost_items_counter"
    
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
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error signing up", e)
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error signing in", e)
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    // Image upload method
    suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val userId = getCurrentUser()?.uid ?: "anonymous"
            val filename = "lostitem_${UUID.randomUUID()}.jpg"
            val fileRef = storage.reference.child("lost_item_images/$userId/$filename")
            
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
    
    // Get next numeric ID from counter document
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
    suspend fun addLostItem(lostItem: LostItem): Result<String> {
        return try {
            val currentUser = getCurrentUser()
            val userId = currentUser?.uid ?: "anonymous"
            val userEmail = currentUser?.email ?: ""
            val numericId = getNextNumericId()
            
            val itemWithIds = lostItem.copy(
                userId = userId,
                userEmail = userEmail,
                numericId = numericId
            )
            
            val docRef = lostItemsCollection.add(itemWithIds).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error adding lost item", e)
            Result.failure(e)
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
            // Get item to check for image
            val itemSnapshot = lostItemsCollection.document(itemId).get().await()
            val item = itemSnapshot.toObject(LostItem::class.java)
            
            // Delete item document
            lostItemsCollection.document(itemId).delete().await()
            
            // If item had an image, delete the image too
            item?.let {
                if (it.imageUrl.isNotEmpty()) {
                    try {
                        // Get storage reference from URL and delete
                        val storageRef = storage.getReferenceFromUrl(it.imageUrl)
                        storageRef.delete().await()
                    } catch (e: Exception) {
                        Log.e("FirebaseManager", "Error deleting image", e)
                        // Continue even if image deletion fails
                    }
                }
            }
            
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
} 