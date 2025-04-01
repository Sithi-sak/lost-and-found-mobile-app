package com.example.lostandfound.model

import com.google.firebase.firestore.DocumentId

data class Chat(
    @DocumentId val id: String = "",
    val participants: List<String> = emptyList(),
    val itemId: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val otherUserName: String? = null,
    val otherUserEmail: String? = null
) 