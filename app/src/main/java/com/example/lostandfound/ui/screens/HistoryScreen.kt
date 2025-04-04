package com.example.lostandfound.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(
    viewModel: LostAndFoundViewModel,
    onNavigateToDetail: (LostItem) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userItems by remember { mutableStateOf<List<LostItem>>(emptyList()) }
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Define refreshTrigger
    val refreshTrigger = remember { mutableStateOf(0) }
    
    // Pull-to-refresh state
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(refreshing, { 
        refreshing = true
        coroutineScope.launch {
            refreshTrigger.value += 1
        }
    })
    
    // Collect user's items
    LaunchedEffect(refreshTrigger.value) {
        viewModel.getUserLostItems().collect { items ->
            userItems = items
        }
    }
    
    // Track loading state
    LaunchedEffect(isLoading) {
        if (!isLoading && refreshing) {
            refreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Posts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading && userItems.isEmpty()) {
                CircularProgressIndicator()
            } else if (userItems.isEmpty()) {
                Text("You haven't posted any items yet")
            } else {
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