package com.example.lostandfound.ui.screens

// Import necessary Android and Compose components for history screen functionality
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.ui.components.LostItemCard
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi

/**
 * HistoryScreen composable that displays a list of items posted by the current user.
 * Features include pull-to-refresh and item status updates.
 *
 * viewModel The ViewModel that manages user items data
 * onNavigateToDetail Callback for viewing item details
 * onNavigateBack Callback for navigating back
 * modifier Optional modifier for the screen layout
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(
    viewModel: LostAndFoundViewModel,
    onNavigateToDetail: (LostItem) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State for storing user's posted items
    var userItems by remember { mutableStateOf<List<LostItem>>(emptyList()) }
    
    // Collect loading state from ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Coroutine scope for async operations
    val coroutineScope = rememberCoroutineScope()
    
    // Trigger for refreshing items
    val refreshTrigger = remember { mutableStateOf(0) }
    
    // Pull-to-refresh state management
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(refreshing, { 
        refreshing = true
        coroutineScope.launch {
            refreshTrigger.value += 1
        }
    })
    
    // Effect to load user's items when refresh trigger changes
    LaunchedEffect(refreshTrigger.value) {
        viewModel.getUserLostItems().collect { items ->
            userItems = items
        }
    }
    
    // Effect to handle loading state for pull-to-refresh
    LaunchedEffect(isLoading) {
        if (!isLoading && refreshing) {
            refreshing = false
        }
    }

    // Main screen layout with Scaffold
    Scaffold(
        topBar = {
            // Top app bar with title
            TopAppBar(
                title = { Text("My Posts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            )
        }
    ) { padding ->
        // Main content area with pull-to-refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            // Show loading indicator during initial load
            if (isLoading && userItems.isEmpty()) {
                CircularProgressIndicator()
            } 
            // Show empty state message if no items
            else if (userItems.isEmpty()) {
                Text("You haven't posted any items yet")
            } 
            // Display list of user's items
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, 
                        end = 16.dp, 
                        top = 12.dp, 
                        bottom = 90.dp  // Increased bottom padding to account for navigation bar
                    ),
                ) {
                    items(userItems) { item ->
                        LostItemCard(
                            item = item,
                            onClick = { onNavigateToDetail(item) },
                            onStatusChange = { newStatus ->
                                viewModel.updateItemStatus(item.id, newStatus)
                            }
                        )
                    }
                }
            }
            
            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = refreshing, 
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
} 