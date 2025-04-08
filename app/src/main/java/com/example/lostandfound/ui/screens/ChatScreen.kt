package com.example.lostandfound.ui.screens

// Import necessary Android and Compose components for chat functionality
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lostandfound.model.Message
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import com.example.lostandfound.viewmodel.AuthState
import com.example.lostandfound.viewmodel.ChatState
import com.example.lostandfound.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ChatScreen composable that displays a conversation between users.
 * Shows messages in a scrollable list with message input at the bottom.
 *
 * chatId The unique identifier of the chat conversation
 * viewModel The ViewModel that manages chat state and message handling
 * onNavigateBack Callback function to navigate back from the chat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit
) {
    // State for the current message being typed
    var messageText by remember { mutableStateOf("") }
    
    // List to store and display messages
    val messages = remember { mutableStateListOf<Message>() }
    
    // Collect authentication state to identify current user
    val authState by viewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.Authenticated)?.user
    
    // Collect chat state for error handling
    val chatState by viewModel.chatState.collectAsState()
    val context = LocalContext.current
    
    // State for controlling message list scrolling
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Sort messages by timestamp for chronological display
    val sortedMessages = messages.sortedBy { it.timestamp }

    // Effect to load messages and handle updates
    LaunchedEffect(chatId) {
        // Initial load of chat messages
        viewModel.getChatMessages(chatId)
        
        // Collect new messages and update the UI
        viewModel.messages.collect { newMessages ->
            messages.clear()
            messages.addAll(newMessages)
            
            // Auto-scroll to the latest message
            if (newMessages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(newMessages.size - 1)
                }
            }
        }
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

    // Main screen layout with top app bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages List - Scrollable container for chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(sortedMessages) { message ->
                    // Determine if message is from current user
                    val isOwnMessage = message.senderId == currentUser?.uid
                    MessageBubble(
                        message = message,
                        isOwnMessage = isOwnMessage
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Message Input Area - Fixed at bottom of screen
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = BorderGrey,
                        shape = Shapes.extraSmall
                    ),
                color = White,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Message input field
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = BorderGrey,
                                shape = Shapes.extraSmall
                            ),
                        placeholder = { Text("Type a message...", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderGrey,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = Shapes.extraSmall
                    )
                    
                    // Send message button
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                // Send message and clear input
                                viewModel.sendMessage(chatId, messageText)
                                messageText = ""
                                
                                // Auto-scroll to bottom after sending
                                coroutineScope.launch {
                                    // Small delay to ensure message is added
                                    kotlinx.coroutines.delay(100)
                                    listState.animateScrollToItem(messages.size)
                                }
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * MessageBubble composable that displays a single message in the chat.
 * Shows different styles for own messages vs. other users' messages.
 *
 * @param message The message to display
 * @param isOwnMessage Boolean indicating if the message is from the current user
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {
            // Sender name display
            Text(
                text = if (isOwnMessage) "You" else message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            
            // Message bubble with different colors for own/other messages
            Surface(
                shape = Shapes.extraSmall,
                color = if (isOwnMessage) 
                    MaterialTheme.colorScheme.primary
                else 
                    LightGray,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .border(
                        width = 1.dp,
                        color = if (isOwnMessage) MaterialTheme.colorScheme.primary else BorderGrey,
                        shape = Shapes.extraSmall
                    )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Message content
                    Text(
                        text = message.content,
                        color = if (isOwnMessage) 
                            White
                        else
                            Color(0xFF212529)
                    )
                    
                    // Timestamp display
                    Text(
                        text = formatTimestamp(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOwnMessage) 
                            White.copy(alpha = 0.7f)
                        else
                            TextGrey,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Helper function to format message timestamps.
 * Converts Unix timestamp to readable time format.
 *
 * timestamp The Unix timestamp to format
 * Formatted time string (HH:mm)
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
} 