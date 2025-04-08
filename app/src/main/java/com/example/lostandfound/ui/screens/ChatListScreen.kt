package com.example.lostandfound.ui.screens

// Import necessary Android and Compose components for chat list functionality
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lostandfound.model.Chat
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import com.example.lostandfound.viewmodel.ChatState
import com.example.lostandfound.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * ChatListScreen composable that displays a list of chat conversations.
 * Shows loading, error, and empty states appropriately.
 *
 * modifier Optional modifier for the screen layout
 * viewModel The ViewModel that manages chat state and data
 * onNavigateToChat Callback function to navigate to a specific chat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    modifier: Modifier = Modifier,
    viewModel: LostAndFoundViewModel,
    onNavigateToChat: (String) -> Unit
) {
    // Collect chat list and state from ViewModel
    val chats by viewModel.chats.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    val context = LocalContext.current

    // Effect to load chats when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.getChats()
    }

    // Effect to handle chat errors
    LaunchedEffect(chatState) {
        if (chatState is ChatState.Error) {
            Toast.makeText(
                context,
                (chatState as ChatState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Main screen layout
    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar with title
        TopAppBar(
            title = { Text("Chats") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = White
            )
        )

        // Handle different chat states
        when (chatState) {
            ChatState.Loading -> {
                // Show loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ChatState.Error -> {
                // Show error message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error loading chats",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                if (chats.isEmpty()) {
                    // Show empty state message
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No chats yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextGrey
                        )
                    }
                } else {
                    // Display list of chats
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    }
}

/**
 * ChatListItem composable that displays a single chat preview.
 * Shows the other user's name, last message, and timestamp.
 *
 * chat The chat data to display
 * onClick Callback function when chat is clicked
 */
@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    // Chat item surface with click handling
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = BorderGrey,
                shape = Shapes.extraSmall
            ),
        shape = Shapes.extraSmall,
        color = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Row containing user name and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Other user's name
                Text(
                    text = chat.otherUserName ?: "User",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF212529)
                )
                // Last message timestamp
                Text(
                    text = formatTimestamp(chat.lastMessageTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
            }
            // Last message preview
            Text(
                text = chat.lastMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGrey
            )
        }
    }
}

/**
 * Helper function to format message timestamps.
 * Shows different formats based on message age:
 * - Less than 24 hours: HH:mm
 * - Less than a week: Day of week (e.g., "Mon")
 * - Older: MM/dd/yy
 *
 * timestamp The Unix timestamp to format
 * Formatted time string based on message age
 */
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