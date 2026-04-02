package com.productivityapp.ui.screens.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productivityapp.data.model.Plan
import com.productivityapp.data.model.ProjectStatus
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskCategory
import com.productivityapp.data.model.TaskStatus
import com.productivityapp.ui.components.ProgressBar
import com.productivityapp.ui.components.StatusChip
import com.productivityapp.ui.screens.tasks.navigateToTaskEdit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlan: (Long) -> Unit,
    onNavigateToGuided: (Long) -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.project?.title ?: "Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToGuided(viewModel.projectId) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Guided Mode")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("添加计划") },
                            onClick = {
                                showMenu = false
                                viewModel.showCreatePlanDialog()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        )
                        if (uiState.project?.status == ProjectStatus.ACTIVE) {
                            DropdownMenuItem(
                                text = { Text("Mark Completed") },
                                onClick = {
                                    showMenu = false
                                    viewModel.updateProjectStatus(ProjectStatus.COMPLETED)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreatePlanDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Plan")
            }
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
                uiState.project?.let { project ->
                    item {
                        ProjectHeader(project = project)
                    }
                    // PRD: 模块3-7 环节进度
                    item {
                        CategoryProgressSection(
                            categoryProgress = uiState.categoryProgress,
                            overallProgress = uiState.overallProgress
                        )
                    }
                }

                if (uiState.plans.isEmpty()) {
                    item {
                        EmptyPlansCard()
                    }
                } else {
                    items(uiState.plans, key = { it.id }) { plan ->
                        PlanCard(
                            plan = plan,
                            tasks = uiState.tasksByPlan[plan.id] ?: emptyList(),
                            onClick = { onNavigateToPlan(plan.id) },
                            onEdit = { viewModel.showEditPlanDialog(plan) },
                            onDelete = { viewModel.deletePlan(plan) }
                        )
                    }
                }
            }
        }

        if (uiState.showPlanDialog) {
            PlanDialog(
                plan = uiState.editingPlan,
                onDismiss = { viewModel.dismissPlanDialog() },
                onConfirm = { title, description ->
                    if (uiState.editingPlan != null) {
                        viewModel.updatePlan(uiState.editingPlan!!, title, description)
                    } else {
                        viewModel.createPlan(title, description)
                    }
                }
            )
        }
    }
}

@Composable
fun ProjectHeader(project: com.productivityapp.data.model.Project) {
    val projectColor = try {
        Color(android.graphics.Color.parseColor(project.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = projectColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = projectColor
                )
                StatusChip(status = project.status)
            }
            if (project.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// PRD: 模块3-7 环节进度展示
@Composable
fun CategoryProgressSection(
    categoryProgress: List<CategoryProgress>,
    overallProgress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("环节进度（模块3-7）", style = MaterialTheme.typography.titleMedium)
                Text(
                    "整体 ${(overallProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 整体进度条
            LinearProgressIndicator(
                progress = overallProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 各环节进度
            categoryProgress.forEach { cp ->
                if (cp.total > 0) { // 只显示有任务的环节
                    CategoryProgressRow(cp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryProgressRow(cp: CategoryProgress) {
    val icon = when (cp.category) {
        TaskCategory.NEW_PRODUCT -> Icons.Default.Description
        TaskCategory.MOLD -> Icons.Default.Build
        TaskCategory.TEST -> Icons.Default.Science
        TaskCategory.DOCUMENT -> Icons.Default.Folder
        TaskCategory.CERTIFICATION -> Icons.Default.VerifiedUser
    }

    val color = when (cp.category) {
        TaskCategory.NEW_PRODUCT -> Color(0xFF4CAF50)
        TaskCategory.MOLD -> Color(0xFF2196F3)
        TaskCategory.TEST -> Color(0xFFFF9800)
        TaskCategory.DOCUMENT -> Color(0xFF9C27B0)
        TaskCategory.CERTIFICATION -> Color(0xFFE91E63)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = cp.category.displayName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
        LinearProgressIndicator(
            progress = cp.progress,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
        Text(
            text = "${cp.completed}/${cp.total}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PlanCard(
    plan: Plan,
    tasks: List<Task>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val progress = if (tasks.isEmpty()) 0f else completedCount.toFloat() / tasks.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }

            if (plan.description.isNotEmpty()) {
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ProgressBar(
                progress = progress,
                label = "$completedCount / ${tasks.size} tasks"
            )
        }
    }
}

@Composable
fun EmptyPlansCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistAdd,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No plans yet",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Add your first plan to break down this project",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlanDialog(
    plan: Plan?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(plan?.title ?: "") }
    var description by remember { mutableStateOf(plan?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (plan == null) "Create Plan" else "Edit Plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, description) },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private val ProjectDetailViewModel.projectId: Long
    get() = uiState.value.project?.id ?: 0L
