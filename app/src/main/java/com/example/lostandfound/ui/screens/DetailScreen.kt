package com.example.lostandfound.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lostandfound.model.ItemStatus
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.ui.theme.LostRed
import com.example.lostandfound.ui.theme.FoundGreen
import com.example.lostandfound.ui.theme.White
import com.example.lostandfound.ui.theme.BorderGrey
import com.example.lostandfound.ui.theme.TextGrey
import com.example.lostandfound.viewmodel.AuthState
import com.example.lostandfound.viewmodel.DetailState
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.clickable
import com.example.lostandfound.ui.theme.DarkGray
import com.example.lostandfound.ui.theme.LightGray
import com.example.lostandfound.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit,
    onDeleteSuccess: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val detailState by viewModel.detailState.collectAsState()
    
    // Fetch item details when the screen is first composed
    LaunchedEffect(itemId) {
        viewModel.fetchLostItemById(itemId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (detailState) {
                        is DetailState.Success -> {
                            val lostItem = (detailState as DetailState.Success).item
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Item Details")
                                if (lostItem.numericId > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "#${lostItem.numericId}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        else -> {
                            Text("Item Details")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (detailState is DetailState.Success) {
                        val lostItem = (detailState as DetailState.Success).item
                        val currentUserId = try {
                            (authState as? AuthState.Authenticated)?.user?.uid
                        } catch (e: Exception) {
                            null
                        }
                        if (lostItem.userId == currentUserId) {
                            IconButton(onClick = { 
                                viewModel.deleteLostItem(itemId)
                                onDeleteSuccess()
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Item")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (detailState) {
                is DetailState.Loading -> {
                    CircularProgressIndicator()
                }
                is DetailState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading item details",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (detailState as DetailState.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
                is DetailState.Success -> {
                    val lostItem = (detailState as DetailState.Success).item
                    val currentUserId = try {
                        (authState as? AuthState.Authenticated)?.user?.uid
                    } catch (e: Exception) {
                        null
                    }
                    val isUsersOwnItem = lostItem.userId == currentUserId
                    
                    ItemDetailContent(
                        lostItem = lostItem,
                        isUsersOwnItem = isUsersOwnItem,
                        viewModel = viewModel,
                        onNavigateToChat = onNavigateToChat
                    )
                }
                else -> {
                    Text("Invalid state")
                }
            }
        }
    }
}

@Composable
private fun ItemDetailContent(
    lostItem: LostItem,
    isUsersOwnItem: Boolean,
    viewModel: LostAndFoundViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = BorderGrey,
                    shape = Shapes.extraSmall
                ),
            shape = Shapes.extraSmall,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightGray
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = lostItem.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF212529)  // Laravel dark text color
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Posted by ${lostItem.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGrey
                    )
                    
                    Text(
                        text = formatDate(lostItem.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Display image if available
                if (lostItem.imageBase64.isNotEmpty()) {
                    val imageBytes = Base64.decode(lostItem.imageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = BorderGrey,
                                shape = Shapes.extraSmall
                            )
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Image of lost item",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status badge
                Surface(
                    color = if (lostItem.status == ItemStatus.LOST) LostRed else FoundGreen,
                    shape = Shapes.extraSmall,
                    modifier = Modifier
                        .clickable(enabled = isUsersOwnItem) {
                            val newStatus = if (lostItem.status == ItemStatus.LOST) {
                                ItemStatus.FOUND
                            } else {
                                ItemStatus.LOST
                            }
                            viewModel.updateItemStatus(lostItem.id, newStatus)
                        }
                ) {
                    Text(
                        text = lostItem.status.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = White
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Description section
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = BorderGrey,
                            shape = Shapes.extraSmall
                        ),
                    shape = Shapes.extraSmall,
                    color = Color(0xFFF8F9FA)  // Laravel light gray background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF212529)
                        )
                        
                        Text(
                            text = lostItem.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp),
                            color = TextGrey
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location section
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = BorderGrey,
                            shape = Shapes.extraSmall
                        ),
                    shape = Shapes.extraSmall,
                    color = Color(0xFFF8F9FA)  // Laravel light gray background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF212529)
                        )
                        
                        Text(
                            text = if (lostItem.location.isNotBlank()) lostItem.location else "No location provided",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            color = if (lostItem.location.isNotBlank()) TextGrey else Color(0xFFADB5BD)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contact section
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = BorderGrey,
                            shape = Shapes.extraSmall
                        ),
                    shape = Shapes.extraSmall,
                    color = Color(0xFFF8F9FA)  // Laravel light gray background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Contact",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF212529)
                        )
                        
                        Text(
                            text = lostItem.contact,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp),
                            color = TextGrey
                        )
                    }
                }
            }
        }
        
        if (!isUsersOwnItem) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${lostItem.contact}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = Shapes.extraSmall
                        ),
                    shape = Shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Call")
                    }
                }
                
                Button(
                    onClick = {
                        viewModel.createOrOpenChat(
                            otherUserId = lostItem.userId,
                            itemId = lostItem.id
                        ) { chatId ->
                            onNavigateToChat(chatId)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = Shapes.extraSmall
                        ),
                    shape = Shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = White
                    ),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Chat")
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
} 