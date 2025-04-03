package com.example.lostandfound.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

enum class ItemStatus {
    LOST,
    FOUND
}

@IgnoreExtraProperties
data class LostItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val contact: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val username: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String = "",
    val numericId: Long = 0, // Numeric ID for alternate indexing
    val location: String = "", // Location description
    val status: ItemStatus = ItemStatus.LOST // Default status is LOST
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "description" to description,
            "contact" to contact,
            "userId" to userId,
            "userEmail" to userEmail,
            "username" to username,
            "timestamp" to timestamp,
            "imageBase64" to imageBase64,
            "numericId" to numericId,
            "location" to location,
            "status" to status.name // Store enum as string
        )
    }
} 