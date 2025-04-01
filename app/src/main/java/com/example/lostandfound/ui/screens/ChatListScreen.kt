package com.example.lostandfound.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lostandfound.model.Chat
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    modifier: Modifier = Modifier,
    viewModel: LostAndFoundViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val chats = remember { mutableStateListOf<Chat>() }

    LaunchedEffect(Unit) {
        // TODO: Implement chat list fetching from Firestore
        // viewModel.getChats().collect { newChats ->
        //     chats.clear()
        //     chats.addAll(newChats)
        // }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        TopAppBar(
            title = { Text("Chats") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        if (chats.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No chats yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Chat list
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(chats) { chat ->
                    ChatListItem(
                        chat = chat,
                        onClick = { onNavigateToChat(chat.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        headlineContent = {
            // TODO: Replace with actual user name
            Text(
                text = "User",
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = chat.lastMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(
                text = formatTimestamp(chat.lastMessageTimestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
    Divider()
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 24 * 60 * 60 * 1000 -> { // Less than 24 hours
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        diff < 7 * 24 * 60 * 60 * 1000 -> { // Less than a week
            SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        }
        else -> {
            SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date(timestamp))
        }
    }
} 