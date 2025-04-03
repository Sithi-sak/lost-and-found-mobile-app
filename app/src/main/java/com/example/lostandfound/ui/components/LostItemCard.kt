package com.example.lostandfound.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lostandfound.model.ItemStatus
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.ui.theme.LightGray
import com.example.lostandfound.ui.theme.Shapes
import java.text.SimpleDateFormat
import java.util.*

// Define Laravel-style colors
private val BorderGrey = Color(0xFFDEE2E6)
private val TextGrey = Color(0xFF6C757D)
private val White = Color(0xFFFFFFFF)
private val LostRed = Color(0xFFDC3545)
private val FoundGreen = Color(0xFF28A745)

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Composable
fun LostItemCard(
    item: LostItem,
    onClick: () -> Unit,
    onStatusChange: ((ItemStatus) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = BorderGrey,
                shape = Shapes.extraSmall
            ),
        shape = Shapes.extraSmall,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF212529)  // Laravel dark text color
                )
                
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

            if (item.numericId > 0) {
                Text(
                    text = "#${item.numericId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Posted by ${item.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
                Text(
                    text = formatDate(item.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
            }
        }
    }
} 