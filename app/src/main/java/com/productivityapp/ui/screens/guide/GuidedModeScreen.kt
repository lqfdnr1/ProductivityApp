package com.productivityapp.ui.screens.guide

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productivityapp.data.model.TaskPriority
import com.productivityapp.ui.components.ProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedModeScreen(
    onNavigateBack: () -> Unit,
    viewModel: GuidedModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.project?.title ?: "Guided Mode")
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.isFinished -> {
                    FinishedContent(
                        completedCount = uiState.completedCount,
                        skippedCount = uiState.skippedCount,
                        encouragingMessage = viewModel.getEncouragingMessage(),
                        onFinish = onNavigateBack
                    )
                }
                uiState.tasks.isEmpty() -> {
                    EmptyContent(onNavigateBack = onNavigateBack)
                }
                else -> {
                    GuidedTaskContent(
                        task = viewModel.getCurrentTask(),
                        currentIndex = uiState.currentTaskIndex,
                        totalCount = uiState.tasks.size,
                        progress = viewModel.getProgress(),
                        encouragingMessage = viewModel.getEncouragingMessage(),
                        onComplete = { viewModel.completeCurrentTask() },
                        onSkip = { viewModel.skipCurrentTask() },
                        onPrevious = { viewModel.goToPreviousTask() }
                    )
                }
            }
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit Guided Mode?") },
                text = {
                    Text("Your progress has been saved. You can resume this session later.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        onNavigateBack()
                    }) {
                        Text("Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Continue")
                    }
                }
            )
        }
    }
}

@Composable
fun GuidedTaskContent(
    task: com.productivityapp.data.model.Task?,
    currentIndex: Int,
    totalCount: Int,
    progress: Float,
    encouragingMessage: String,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress section
        Text(
            text = "Task ${currentIndex + 1} of $totalCount",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = encouragingMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Task card
        task?.let {
            TaskGuidedCard(
                task = it,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                enabled = currentIndex > 0
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Previous")
            }

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Skip")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mark Complete", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun TaskGuidedCard(
    task: com.productivityapp.data.model.Task,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Priority indicator
            val priorityColor = when (task.priority) {
                TaskPriority.HIGH -> Color(0xFFE53935)
                TaskPriority.MEDIUM -> Color(0xFFFFA000)
                TaskPriority.LOW -> Color(0xFF43A047)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = priorityColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = when (task.priority) {
                        TaskPriority.HIGH -> "🔴 High Priority"
                        TaskPriority.MEDIUM -> "🟡 Medium Priority"
                        TaskPriority.LOW -> "🟢 Low Priority"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = priorityColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            task.dueDate?.let { dueDate ->
                Spacer(modifier = Modifier.height(16.dp))
                val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Due: ${dateFormat.format(java.util.Date(dueDate))}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinishedContent(
    completedCount: Int,
    skippedCount: Int,
    encouragingMessage: String,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "🎉 Congratulations!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = encouragingMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completedCount",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$skippedCount",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Skipped",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Finish", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun EmptyContent(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "All Done!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You've completed all tasks in this project.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Go Back", style = MaterialTheme.typography.titleMedium)
        }
    }
}
