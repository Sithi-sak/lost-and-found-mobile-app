package com.example.lostandfound.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val email: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "email" to email,
            "username" to username,
            "phoneNumber" to phoneNumber,
            "createdAt" to createdAt
        )
    }
} 