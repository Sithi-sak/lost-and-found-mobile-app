package com.example.lostandfound.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val authState by viewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.Authenticated)?.user
    val chatState by viewModel.chatState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(chatId) {
        viewModel.getChatMessages(chatId)
        viewModel.messages.collect { newMessages ->
            messages.clear()
            messages.addAll(newMessages)
        }
    }

    // Show error toast if chat state is error
    LaunchedEffect(chatState) {
        if (chatState is ChatState.Error) {
            Toast.makeText(
                context,
                (chatState as ChatState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(messages) { message ->
                    val isOwnMessage = message.senderId == currentUser?.uid
                    MessageBubble(
                        message = message,
                        isOwnMessage = isOwnMessage
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Message Input
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
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(chatId, messageText)
                                messageText = ""
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

@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
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
                if (!isOwnMessage) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOwnMessage) 
                            White.copy(alpha = 0.7f)
                        else
                            TextGrey
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = message.content,
                    color = if (isOwnMessage) 
                        White
                    else
                        Color(0xFF212529)
                )
            }
        }
    }
} 