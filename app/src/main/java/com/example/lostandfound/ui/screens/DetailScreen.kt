package com.example.lostandfound.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.Message
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.viewmodel.AuthState
import com.example.lostandfound.viewmodel.DetailState
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit,
    onDeleteSuccess: () -> Unit
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
                        isUsersOwnItem = isUsersOwnItem
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
    isUsersOwnItem: Boolean
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = lostItem.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Posted on ${formatDate(lostItem.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                // Display image if available
                if (lostItem.imageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(lostItem.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image of lost item",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4/3f)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status badge (placeholder for future feature)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Active",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = lostItem.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location section (placeholder for future feature)
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Location data not available yet",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Contact",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = lostItem.contact,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        if (!isUsersOwnItem) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First row - Call and SMS
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Call")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:${lostItem.contact}")
                                putExtra("sms_body", "Hi, I'm contacting about your lost item: ${lostItem.title}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Message")
                    }
                }
                
                // Second row - Email and Chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email button
                    OutlinedButton(
                        onClick = {
                            try {
                                // Use userEmail if available, otherwise try to use contact
                                val emailAddress = if (lostItem.userEmail.isNotEmpty()) 
                                                    lostItem.userEmail 
                                                  else 
                                                    lostItem.contact
                                
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                                    putExtra(Intent.EXTRA_SUBJECT, "About your lost item: ${lostItem.title}")
                                    putExtra(Intent.EXTRA_TEXT, "Hi, I'm contacting you regarding your lost item listing on the Lost and Found app.")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle the case where there's no email app
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Email")
                    }
                    
                    // In-app Chat button
                    Button(
                        onClick = {
                            // This would typically navigate to a chat screen
                            // You can implement this in future updates
                            Toast.makeText(
                                context,
                                "Chat feature coming soon!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
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