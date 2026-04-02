package com.productivityapp.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskStatus
import com.productivityapp.ui.components.ProjectCard
import com.productivityapp.ui.components.TaskCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProject: (Long) -> Unit,
    onNavigateToGuided: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productivity") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "最近项目",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (uiState.recentProjects.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "暂无项目",
                            subtitle = "创建你的第一个项目开始吧"
                        )
                    }
                } else {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.recentProjects) { project ->
                                RecentProjectCard(
                                    project = project,
                                    onClick = { onNavigateToProject(project.id) },
                                    onStartGuided = { onNavigateToGuided(project.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "今日任务",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (uiState.todayTasks.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "今天没有任务",
                            subtitle = "好好休息吧！"
                        )
                    }
                } else {
                    items(uiState.todayTasks) { task ->
                        TaskCard(
                            task = task,
                            onClick = { },
                            onStatusChange = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentProjectCard(
    project: Project,
    onClick: () -> Unit,
    onStartGuided: () -> Unit
) {
    val projectColor = try {
        Color(android.graphics.Color.parseColor(project.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = projectColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = projectColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = project.description.ifEmpty { "No description" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = onStartGuided,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = projectColor.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("开始", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
