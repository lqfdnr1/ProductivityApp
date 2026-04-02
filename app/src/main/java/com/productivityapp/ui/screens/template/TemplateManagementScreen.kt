package com.productivityapp.ui.screens.template

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productivityapp.ui.screens.guide.TaskTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success snackbar when save succeeds
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("✅ 模板已保存")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模板管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 保存按钮
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.saveCustomizations() }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "保存模板")
                        }
                    }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置全部")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 说明
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("模板自定义", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "调整默认工期和前置任务关系，修改后将影响新建项目的任务排程。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "💡 修改后请点击顶部 💾 保存按钮来保存您的更改。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 任务列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.allTasks) { task ->
                    TaskTemplateCard(
                        task = task,
                        hasCustomization = task.id in uiState.customDurations || task.id in uiState.customPrerequisites,
                        onClick = { viewModel.selectTask(task) }
                    )
                }
            }
        }

        // 任务编辑对话框
        if (uiState.selectedTask != null) {
            TaskEditDialog(
                task = uiState.selectedTask!!,
                allTasks = uiState.allTasks,
                onDismiss = { viewModel.dismissEditor() },
                onDurationChange = { duration -> viewModel.updateTaskDuration(uiState.selectedTask!!.id, duration) },
                onAddPrerequisite = { prereqId -> viewModel.addPrerequisite(uiState.selectedTask!!.id, prereqId) },
                onRemovePrerequisite = { prereqId -> viewModel.removePrerequisite(uiState.selectedTask!!.id, prereqId) },
                onReset = { viewModel.resetToDefault(uiState.selectedTask!!.id) }
            )
        }

        // 重置确认对话框
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("重置全部") },
                text = { Text("确定要将所有模板恢复为默认设置吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetAllToDefault()
                        showResetDialog = false
                    }) {
                        Text("确定重置")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
fun TaskTemplateCard(
    task: TaskTemplate,
    hasCustomization: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${task.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "工期: ${task.duration}天",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (task.prerequisites.isNotEmpty()) {
                        Text(
                            text = "前置: ${task.prerequisites.size}个",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (hasCustomization) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "已自定义",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    task: TaskTemplate,
    allTasks: List<TaskTemplate>,
    onDismiss: () -> Unit,
    onDurationChange: (Int) -> Unit,
    onAddPrerequisite: (Int) -> Unit,
    onRemovePrerequisite: (Int) -> Unit,
    onReset: () -> Unit
) {
    var showAddPrereqDialog by remember { mutableStateOf(false) }
    val availablePrereqs = allTasks.filter {
        it.id != task.id && it.id !in task.prerequisites
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${task.id}. ${task.name}")
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "重置")
                }
            }
        },
        text = {
            Column {
                // 工期调整
                Text("默认工期", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (task.duration > 1) onDurationChange(task.duration - 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "减少")
                    }
                    Text(
                        text = "${task.duration} 天",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(60.dp)
                    )
                    IconButton(
                        onClick = { onDurationChange(task.duration + 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "增加")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Spacer(modifier = Modifier.height(16.dp))

                // 前置任务
                Text("前置任务", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (task.prerequisites.isEmpty()) {
                    Text(
                        "无前置任务（可独立开始）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column {
                        task.prerequisites.forEach { prereqId ->
                            val prereqTask = allTasks.find { it.id == prereqId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$prereqId. ${prereqTask?.name ?: "未知"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(
                                    onClick = { onRemovePrerequisite(prereqId) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "移除",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { showAddPrereqDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加前置任务")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )

    // 添加前置任务对话框
    if (showAddPrereqDialog) {
        AlertDialog(
            onDismissRequest = { showAddPrereqDialog = false },
            title = { Text("选择前置任务") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(availablePrereqs) { prereqTask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddPrerequisite(prereqTask.id)
                                    showAddPrereqDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${prereqTask.id}. ${prereqTask.name}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddPrereqDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
