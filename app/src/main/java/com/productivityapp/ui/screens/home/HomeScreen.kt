package com.productivityapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskCategory
import com.productivityapp.data.model.TaskStatus
import com.productivityapp.ui.components.TaskCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProject: (Long) -> Unit,
    onNavigateToProjectSetup: () -> Unit,
    onNavigateToGuided: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("制造业项目管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PRD: 项目总览卡片
            item {
                Text("📊 项目总览", style = MaterialTheme.typography.titleMedium)
            }

            if (uiState.activeProjects.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = "暂无进行中的项目",
                        subtitle = "点击下方按钮创建第一个项目",
                        action = {
                            FilledTonalButton(onClick = onNavigateToProjectSetup) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("新建项目")
                            }
                        }
                    )
                }
            } else {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.activeProjects) { project ->
                            ProjectOverviewCard(
                                project = project,
                                progress = uiState.projectProgress[project.id] ?: 0f,
                                onClick = { onNavigateToProject(project.id) }
                            )
                        }
                    }
                }
            }

            // PRD: 5环节进度快捷查看
            if (uiState.activeProjects.isNotEmpty()) {
                item {
                    Text("🔗 环节进度", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    CategoryOverviewRow(
                        categoryStats = uiState.categoryStats,
                        projectCount = uiState.activeProjects.size
                    )
                }
            }

            // PRD: 今日任务
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋 今日任务", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${uiState.todayTasks.count { it.status == TaskStatus.COMPLETED }}/${uiState.todayTasks.size} 完成",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState.todayTasks.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = "今天没有任务",
                        subtitle = "好好休息吧！"
                    )
                }
            } else {
                // 按项目分组显示今日任务
                val tasksByProject = uiState.todayTasks.groupBy { it.planId }
                tasksByProject.forEach { (planId, tasks) ->
                    item {
                        Text(
                            uiState.planProjectNames[planId] ?: "项目任务",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(tasks.take(3)) { task ->
                        TaskCard(
                            task = task,
                            onClick = { },
                            onStatusChange = { },
                            isBlocked = false
                        )
                    }
                    if (tasks.size > 3) {
                        item {
                            Text(
                                "还有 ${tasks.size - 3} 个任务...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 快捷操作
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("⚡ 快捷操作", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Add,
                        label = "新建项目",
                        color = Color(0xFF4CAF50),
                        onClick = onNavigateToProjectSetup,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.PlayArrow,
                        label = "快速开始",
                        color = Color(0xFF2196F3),
                        onClick = {
                            uiState.activeProjects.firstOrNull()?.let { onNavigateToGuided(it.id) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// PRD: 项目总览卡片
@Composable
fun ProjectOverviewCard(
    project: Project,
    progress: Float,
    onClick: () -> Unit
) {
    val projectColor = try {
        Color(android.graphics.Color.parseColor(project.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = projectColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = projectColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = projectColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = projectColor,
                trackColor = projectColor.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = project.description.ifEmpty { "制造业产品开发项目" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// PRD: 5环节概览行
@Composable
fun CategoryOverviewRow(
    categoryStats: Map<TaskCategory, Pair<Int, Int>>, // category -> (completed, total)
    projectCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskCategory.entries.forEach { category ->
                val (completed, total) = categoryStats[category] ?: (0 to 0)
                val progress = if (total > 0) completed.toFloat() / total else 0f
                val color = when (category) {
                    TaskCategory.NEW_PRODUCT -> Color(0xFF4CAF50)
                    TaskCategory.MOLD -> Color(0xFF2196F3)
                    TaskCategory.TEST -> Color(0xFFFF9800)
                    TaskCategory.DOCUMENT -> Color(0xFF9C27B0)
                    TaskCategory.CERTIFICATION -> Color(0xFFE91E63)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(60.dp)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxSize(),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f),
                            strokeWidth = 3.dp,
                        )
                        Icon(
                            imageVector = when (category) {
                                TaskCategory.NEW_PRODUCT -> Icons.Default.Description
                                TaskCategory.MOLD -> Icons.Default.Build
                                TaskCategory.TEST -> Icons.Default.Science
                                TaskCategory.DOCUMENT -> Icons.Default.Folder
                                TaskCategory.CERTIFICATION -> Icons.Default.VerifiedUser
                            },
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (category) {
                            TaskCategory.NEW_PRODUCT -> "新品"
                            TaskCategory.MOLD -> "模具"
                            TaskCategory.TEST -> "试验"
                            TaskCategory.DOCUMENT -> "资料"
                            TaskCategory.CERTIFICATION -> "认证"
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "$completed/$total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    subtitle: String,
    action: @Composable (() -> Unit)? = null
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
            if (action != null) {
                Spacer(modifier = Modifier.height(12.dp))
                action()
            }
        }
    }
}
