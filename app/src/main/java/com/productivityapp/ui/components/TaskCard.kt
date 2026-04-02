package com.productivityapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskPriority
import com.productivityapp.data.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val isSkipped = task.status == TaskStatus.SKIPPED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (!isCompleted && !isSkipped) {
                        onStatusChange(TaskStatus.COMPLETED)
                    } else {
                        onStatusChange(TaskStatus.PENDING)
                    }
                }
            ) {
                Icon(
                    imageVector = if (isCompleted || isSkipped)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle status",
                    tint = when {
                        isCompleted -> Color(0xFF4CAF50)
                        isSkipped -> Color(0xFF9E9E9E)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isCompleted || isSkipped)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None,
                    color = if (isCompleted || isSkipped)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriorityChip(priority = task.priority)
                    task.dueDate?.let { date ->
                        DueDateChip(date = date)
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityChip(priority: TaskPriority) {
    val (text, color) = when (priority) {
        TaskPriority.HIGH -> "High" to Color(0xFFE53935)
        TaskPriority.MEDIUM -> "Medium" to Color(0xFFFFA000)
        TaskPriority.LOW -> "Low" to Color(0xFF43A047)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun DueDateChip(date: Long) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateString = dateFormat.format(Date(date))

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = dateString,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
