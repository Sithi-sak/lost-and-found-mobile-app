package com.example.lostandfound.ui.components

// Import necessary Android and Compose components for image handling and UI construction
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lostandfound.model.ItemStatus
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Define Laravel-style colors for consistent theming
private val BorderGrey = Color(0xFFDEE2E6)  // Light border color
private val TextGrey = Color(0xFF6C757D)    // Secondary text color
private val White = Color(0xFFFFFFFF)       // Background color
private val LostRed = Color(0xFFDC3545)     // Status color for lost items
private val FoundGreen = Color(0xFF28A745)  // Status color for found items

/**
 * Helper function to format timestamp into a readable date string
 * timestamp The Unix timestamp to format
 * Formatted date string (e.g., "Jan 01, 2023")
 */
private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

/**
 * Composable function that displays a lost/found item in a card format.
 * Shows item image, title, status, description, and metadata.
 * 
 * item The LostItem to display
 * onClick Callback when the card is clicked
 * onStatusChange Optional callback for status toggle (only available for item owner)
 * modifier Optional modifier for the card
 */
@Composable
fun LostItemCard(
    item: LostItem,
    onClick: () -> Unit,
    onStatusChange: ((ItemStatus) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Main card container with border and click handling
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = BorderGrey,
                shape = RoundedCornerShape(0.dp)
            ),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {
        // Horizontal layout for image and content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image preview section
            if (item.imageBase64.isNotEmpty()) {
                // Convert Base64 string to bitmap and display
                val imageBytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                
                // Image container with border
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(
                            width = 1.dp,
                            color = BorderGrey,
                            shape = RoundedCornerShape(0.dp)
                        )
                ) {
                    // Display the image with crop scaling
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Preview image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Content section (title, status, description, metadata)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title and status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Item title with ellipsis for overflow
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF212529),  // Laravel dark text color
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Status badge with toggle functionality
                    Surface(
                        color = if (item.status == ItemStatus.LOST) LostRed else FoundGreen,
                        shape = Shapes.extraSmall,
                        contentColor = White,
                        modifier = Modifier
                            .clickable(
                                enabled = onStatusChange != null,
                                onClick = {
                                    onStatusChange?.invoke(
                                        if (item.status == ItemStatus.LOST) ItemStatus.FOUND else ItemStatus.LOST
                                    )
                                }
                            )
                    ) {
                        Text(
                            text = item.status.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Item description with ellipsis for overflow
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGrey,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Item ID display
                if (item.numericId > 0) {
                    Text(
                        text = "#${item.numericId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Metadata row (posted by and date)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Posted by information
                    Text(
                        text = "Posted by ${item.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey
                    )
                    // Formatted date
                    Text(
                        text = formatDate(item.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey
                    )
                }
            }
        }
    }
} 