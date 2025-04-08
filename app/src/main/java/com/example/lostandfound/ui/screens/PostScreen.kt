package com.example.lostandfound.ui.screens

// Import necessary Android and Compose components for image handling and UI
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lostandfound.viewmodel.ImageState
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import com.example.lostandfound.viewmodel.PostState
import androidx.compose.foundation.BorderStroke
import com.example.lostandfound.ui.theme.Shapes

/**
 * PostScreen composable that allows users to create a new lost item post.
 * Provides form fields for title, description, contact info, location, and image upload.
 *
 * param viewModel The ViewModel that manages post creation and state
 * param onNavigateBack Callback function to navigate back after successful post creation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit
) {
    // State variables for form fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    // Get Android context for content resolver access
    val context = LocalContext.current
    
    // Collect states from ViewModel
    val postState by viewModel.postState.collectAsState()
    val imageState by viewModel.imageState.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()

    // Auto-fill contact field with user's phone number if available
    LaunchedEffect(userPhone) {
        if (contact.isEmpty() && userPhone.isNotEmpty()) {
            contact = userPhone
        }
    }

    // Activity result launcher for image selection
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            inputStream?.let { stream ->
                viewModel.handleImageSelection(stream)
            }
        }
    }

    // Handle post success and navigate back
    LaunchedEffect(postState) {
        if (postState is PostState.Success) {
            onNavigateBack()
            viewModel.resetPostState()
            viewModel.resetImageState()
        }
    }

    // Main screen layout with top app bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Lost Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        // Scrollable form content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title input field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.extraSmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )

            // Description input field with larger height
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = Shapes.extraSmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Contact information input field (pre-filled with user's phone)
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Contact Information") },
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.extraSmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )

            // Location input field
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.extraSmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )

            // Image preview - only displayed if an image has been selected
            if (imageState is ImageState.Success) {
                // Decode Base64 string to bitmap for display
                val imageBytes = Base64.decode((imageState as ImageState.Success).base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // Image selection button
            OutlinedButton(
                onClick = { launcher.launch("image/*") }, // Open system file picker for images
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.extraSmall,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Button text changes based on image state
                    Text(
                        when (imageState) {
                            is ImageState.Success -> "Change Image"
                            is ImageState.Loading -> "Loading..."
                            is ImageState.Error -> "Error - Try Again"
                            else -> "Select Image"
                        }
                    )
                }
            }

            // Post submission button
            Button(
                onClick = {
                    // Extract Base64 image if available
                    val base64Image = when (imageState) {
                        is ImageState.Success -> (imageState as ImageState.Success).base64String
                        else -> ""
                    }
                    // Call ViewModel to create the lost item
                    viewModel.createLostItem(title, description, contact, location, base64Image)
                },
                // Button is enabled only when all required fields are filled
                enabled = title.isNotBlank() && description.isNotBlank() && contact.isNotBlank() && location.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.extraSmall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Button text changes based on post state
                    Text(
                        when (postState) {
                            is PostState.Loading -> "Posting..."
                            is PostState.Error -> "Error - Try Again"
                            else -> "Post"
                        }
                    )
                }
            }

            // Error message display
            if (postState is PostState.Error) {
                Text(
                    text = (postState as PostState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 