package com.example.lostandfound.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.ui.components.LostItemCard
import com.example.lostandfound.viewmodel.AuthState
import com.example.lostandfound.viewmodel.LostAndFoundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: LostAndFoundViewModel,
    onNavigateToDetail: (LostItem) -> Unit,
    onNavigateToPost: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userItems by remember { mutableStateOf<List<LostItem>>(emptyList()) }
    
    // Check for authentication
    LaunchedEffect(authState) {
        if (authState !is AuthState.Authenticated) {
            errorMessage = "You must be logged in to view your posts"
            isLoading = false
        } else {
            isLoading = false
        }
    }
    
    // Safely collect user items
    LaunchedEffect(Unit) {
        try {
            viewModel.getUserLostItems().collect { items ->
                userItems = items
                isLoading = false
                // Clear error message if we successfully got items
                errorMessage = null
            }
        } catch (e: Exception) {
            Log.e("HistoryScreen", "Error collecting user items", e)
            errorMessage = "Error loading your posts. Please try again later."
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Posts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPost,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Lost Item",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(errorMessage!!)
                userItems.isEmpty() -> Text("You haven't posted any items yet.")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(userItems) { item ->
                            LostItemCard(
                                lostItem = item,
                                onClick = { onNavigateToDetail(item) }
                            )
                        }
                    }
                }
            }
        }
    }
} 