package com.productivityapp.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productivityapp.data.model.TaskCategory
import com.productivityapp.data.model.TaskPriority
import com.productivityapp.data.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaskEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPreTaskMenu by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.dueDate
    )

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.task == null) "新建任务" else "编辑任务") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.task != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.saveTask() },
                        enabled = uiState.title.isNotBlank() && !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("任务名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("任务描述") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                // PRD: 任务类别选择
                Text("所属环节（模块3-7）", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = uiState.category.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = {
                            Icon(
                                imageVector = when (uiState.category) {
                                    TaskCategory.NEW_PRODUCT -> Icons.Default.Description
                                    TaskCategory.MOLD -> Icons.Default.Build
                                    TaskCategory.TEST -> Icons.Default.Science
                                    TaskCategory.DOCUMENT -> Icons.Default.Folder
                                    TaskCategory.CERTIFICATION -> Icons.Default.VerifiedUser
                                },
                                contentDescription = null
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        TaskCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(category.displayName, style = MaterialTheme.typography.bodyLarge)
                                        Text(category.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    viewModel.updateCategory(category)
                                    showCategoryMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (category) {
                                            TaskCategory.NEW_PRODUCT -> Icons.Default.Description
                                            TaskCategory.MOLD -> Icons.Default.Build
                                            TaskCategory.TEST -> Icons.Default.Science
                                            TaskCategory.DOCUMENT -> Icons.Default.Folder
                                            TaskCategory.CERTIFICATION -> Icons.Default.VerifiedUser
                                        },
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

                Divider()

                // PRD: 前置任务选择
                Text("前置任务依赖", style = MaterialTheme.typography.labelMedium)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!uiState.canStart && uiState.preTaskId != null)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "前置任务",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    if (uiState.preTaskId == null)
                                        "无前置任务（可独立开始）"
                                    else
                                        "任务 #${uiState.preTaskId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (uiState.preTaskId != null) {
                                IconButton(onClick = { viewModel.updatePreTaskId(null) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除前置任务")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!uiState.canStart && uiState.preTaskId != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "⚠️ 前置任务未完成，任务被阻塞无法启动",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else if (uiState.preTaskId != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "✅ 前置任务已完成，任务可以启动",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "💡 设置前置任务后，必须等前置任务100%完成，此任务才会解锁可操作状态",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                // Due Date
                Text("截止日期", style = MaterialTheme.typography.labelMedium)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.dueDate?.let {
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                                } ?: "未设置",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        if (uiState.dueDate != null) {
                            IconButton(onClick = { viewModel.updateDueDate(null) }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除日期")
                            }
                        }
                    }
                }

                // Priority
                Text("优先级", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = uiState.priority == priority,
                            onClick = { viewModel.updatePriority(priority) },
                            label = {
                                Text(
                                    when (priority) {
                                        TaskPriority.LOW -> "低"
                                        TaskPriority.MEDIUM -> "中"
                                        TaskPriority.HIGH -> "高"
                                    }
                                )
                            },
                            leadingIcon = if (uiState.priority == priority) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                // Status (only for existing tasks)
                if (uiState.task != null) {
                    Text("任务状态", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskStatus.entries.filter { it != TaskStatus.SKIPPED && it != TaskStatus.BLOCKED }.forEach { status ->
                            FilterChip(
                                selected = uiState.status == status,
                                onClick = { viewModel.updateStatus(status) },
                                enabled = status != TaskStatus.BLOCKED,
                                label = {
                                    Text(
                                        when (status) {
                                            TaskStatus.PENDING -> "待处理"
                                            TaskStatus.IN_PROGRESS -> "进行中"
                                            TaskStatus.COMPLETED -> "已完成"
                                            TaskStatus.SKIPPED -> "已跳过"
                                            TaskStatus.BLOCKED -> "已阻塞"
                                        }
                                    )
                                },
                                leadingIcon = if (uiState.status == status) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateDueDate(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("取消")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("删除任务") },
                text = { Text("确定要删除此任务吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTask()
                    }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

fun navigateToTaskEdit(taskId: Long?, planId: Long): String {
    return "task_edit?taskId=${taskId ?: -1}&planId=$planId"
}
