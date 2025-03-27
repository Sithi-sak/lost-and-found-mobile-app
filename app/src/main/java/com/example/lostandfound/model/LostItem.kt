package com.example.lostandfound.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class LostItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val contact: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String = "",
    val numericId: Long = 0 // Numeric ID for alternate indexing
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "description" to description,
            "contact" to contact,
            "userId" to userId,
            "userEmail" to userEmail,
            "timestamp" to timestamp,
            "imageUrl" to imageUrl,
            "numericId" to numericId
        )
    }
} 