package com.example.lostandfound.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lostandfound.ui.theme.Shapes
import com.example.lostandfound.viewmodel.AuthState
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import com.example.lostandfound.viewmodel.ProfileUpdateState

/**
 * Main ProfileScreen composable that displays user information and provides profile management options.
 * 
 * modifier Optional modifier for the screen layout
 * viewModel ViewModel instance for managing profile data and state
 * onNavigateToHistory Callback for navigating to user's history
 * onNavigateToSettings Callback for navigating to settings
 * onLogout Callback for handling user logout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: LostAndFoundViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    // Collect state from ViewModel
    val authState by viewModel.authState.collectAsState()
    val userEmail = if (authState is AuthState.Authenticated) {
        (authState as AuthState.Authenticated).user.email ?: "No email"
    } else {
        "Not signed in"
    }
    val userName by viewModel.userName.collectAsState()
    Log.d("ProfileScreen", "Username from ViewModel: $userName")
    val userPhone by viewModel.userPhone.collectAsState()
    val profileUpdateState by viewModel.profileUpdateState.collectAsState()
    
    // State for edit dialog and form fields
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    
    // Reset edit fields when dialog is opened
    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            editedName = userName
            editedPhone = userPhone
        }
    }
    
    // Handle profile update success
    LaunchedEffect(profileUpdateState) {
        if (profileUpdateState is ProfileUpdateState.Success) {
            showEditDialog = false
            viewModel.resetProfileUpdateState()
        }
    }

    // Main content column
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header Section with user information
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF5F5F5),
            shape = Shapes.extraSmall
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture with placeholder icon
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    color = Color(0xFFE74C3C)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.padding(16.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display user name if available
                if (userName.isNotEmpty()) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display user email
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
                
                // Display user phone if available
                if (userPhone.isNotEmpty()) {
                    Text(
                        text = userPhone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Edit Profile Button
                TextButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.extraSmall
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }
        }

        // Menu Items Section with settings and logout options
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Settings menu item
            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = onNavigateToSettings
            )
            
            // Divider between menu items
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
            
            // Logout menu item
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Logout",
                onClick = onLogout
            )
        }
    }
    
    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (profileUpdateState !is ProfileUpdateState.Loading) {
                    showEditDialog = false 
                }
            },
            title = { 
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                ) 
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Name input field
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        color = Color(0xFFF5F5F5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            placeholder = { Text("JohnDoe") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.dp, Color.Transparent),
                            shape = RoundedCornerShape(0.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone input field
                    Text(
                        text = "Phone Number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        color = Color(0xFFF5F5F5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        OutlinedTextField(
                            value = editedPhone,
                            onValueChange = { editedPhone = it },
                            placeholder = { Text("01234567") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.dp, Color.Transparent),
                            shape = RoundedCornerShape(0.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true
                        )
                    }
                    
                    // Error message display
                    if (profileUpdateState is ProfileUpdateState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (profileUpdateState as ProfileUpdateState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(0.dp),
            confirmButton = {
                // Save button with loading state
                Button(
                    onClick = {
                        viewModel.updateUserProfile(editedName, editedPhone)
                    },
                    enabled = profileUpdateState !is ProfileUpdateState.Loading && 
                        editedName.isNotBlank() && editedPhone.isNotBlank(),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE74C3C),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    if (profileUpdateState is ProfileUpdateState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                // Cancel button
                TextButton(
                    onClick = { showEditDialog = false },
                    enabled = profileUpdateState !is ProfileUpdateState.Loading,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFE74C3C)
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Reusable menu item component for the profile screen.
 * 
 * icon The icon to display
 * title The text to display
 * onClick Callback when the item is clicked
 */
@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = Color.DarkGray
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray
        )
    }
} 