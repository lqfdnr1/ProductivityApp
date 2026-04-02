package com.productivityapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
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
import com.productivityapp.data.model.TaskCategory
import com.productivityapp.data.model.TaskPriority
import com.productivityapp.data.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier,
    isBlocked: Boolean = false // PRD: 前置任务未完成，任务被阻塞
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val isSkipped = task.status == TaskStatus.SKIPPED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = if (isBlocked)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
        else
            CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态图标
            IconButton(
                onClick = {
                    if (isBlocked) return@IconButton
                    if (!isCompleted && !isSkipped) {
                        onStatusChange(TaskStatus.COMPLETED)
                    } else {
                        onStatusChange(TaskStatus.PENDING)
                    }
                },
                enabled = !isBlocked
            ) {
                Icon(
                    imageVector = when {
                        isBlocked -> Icons.Default.Lock
                        isCompleted || isSkipped -> Icons.Default.CheckCircle
                        else -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = "Toggle status",
                    tint = when {
                        isBlocked -> MaterialTheme.colorScheme.error
                        isCompleted -> Color(0xFF4CAF50)
                        isSkipped -> Color(0xFF9E9E9E)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // PRD: 类别标签
                    CategoryChip(category = task.category)
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (isCompleted || isSkipped)
                            TextDecoration.LineThrough
                        else
                            TextDecoration.None,
                        color = when {
                            isBlocked -> MaterialTheme.colorScheme.onSurfaceVariant
                            isCompleted || isSkipped -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // PRD: 阻塞提示
                if (isBlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "前置任务未完成，无法启动",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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

// PRD: 任务类别标签
@Composable
fun CategoryChip(category: TaskCategory) {
    val (text, color) = when (category) {
        TaskCategory.NEW_PRODUCT -> "新品" to Color(0xFF4CAF50)
        TaskCategory.MOLD -> "模具" to Color(0xFF2196F3)
        TaskCategory.TEST -> "试验" to Color(0xFFFF9800)
        TaskCategory.DOCUMENT -> "资料" to Color(0xFF9C27B0)
        TaskCategory.CERTIFICATION -> "认证" to Color(0xFFE91E63)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

@Composable
fun PriorityChip(priority: TaskPriority) {
    val (text, color) = when (priority) {
        TaskPriority.HIGH -> "高" to Color(0xFFE53935)
        TaskPriority.MEDIUM -> "中" to Color(0xFFFFA000)
        TaskPriority.LOW -> "低" to Color(0xFF43A047)
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
