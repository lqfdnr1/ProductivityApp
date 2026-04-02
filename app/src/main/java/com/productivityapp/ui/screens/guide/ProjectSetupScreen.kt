package com.productivityapp.ui.screens.guide

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSetupScreen(
    onNavigateBack: () -> Unit,
    onProjectCreated: (Long) -> Unit,
    viewModel: ProjectSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.createdProjectId) {
        uiState.createdProjectId?.let { projectId ->
            onProjectCreated(projectId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建项目") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    if (uiState.step > 0 && uiState.step < 3) {
                        TextButton(onClick = { viewModel.previousStep() }) {
                            Text("上一步")
                        }
                    }
                    if (uiState.step == 3) {
                        IconButton(onClick = {
                            val csv = viewModel.generateCSVExport()
                            shareText(context, csv, "项目计划_${uiState.projectName}.csv")
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "导出")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 步骤指示器
            StepProgressBar(currentStep = uiState.step)

            when (uiState.step) {
                0 -> Step1_ProjectType(
                    onSelect = { viewModel.selectProductClass(it) }
                )
                1 -> Step2_Options(
                    hasMold = uiState.hasMold,
                    isIoT = uiState.isIoT,
                    isGEA = uiState.isGEA,
                    onHasMoldChange = { viewModel.updateHasMold(it) },
                    onIsIoTChange = { viewModel.updateIsIoT(it) },
                    onIsGEAChange = { viewModel.updateIsGEA(it) },
                    onNext = { viewModel.nextStep() }
                )
                2 -> Step3_ProjectInfo(
                    projectName = uiState.projectName,
                    projectDescription = uiState.projectDescription,
                    startDate = uiState.startDate,
                    onNameChange = { viewModel.updateProjectName(it) },
                    onDescriptionChange = { viewModel.updateProjectDescription(it) },
                    onStartDateChange = { viewModel.updateStartDate(it) },
                    onNext = { viewModel.nextStep() }
                )
                3 -> Step4_Confirm(
                    projectName = uiState.projectName,
                    scheduledTasks = uiState.scheduledTasks,
                    selectedTasks = uiState.selectedTasks,
                    onTaskDurationChange = { id, duration -> viewModel.updateTaskDuration(id, duration) },
                    onCreate = { viewModel.createProject() },
                    isCreating = uiState.isCreating,
                    onRecalculate = { viewModel.updateStartDate(uiState.startDate) }
                )
            }
        }
    }
}

@Composable
fun StepProgressBar(currentStep: Int) {
    val steps = listOf("项目类型", "配置选项", "项目信息", "确认生成")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                index < currentStep -> MaterialTheme.colorScheme.primary
                                index == currentStep -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentStep) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (index == currentStep) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    step,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index <= currentStep) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .height(2.dp)
                        .background(
                            if (index < currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

@Composable
fun Step1_ProjectType(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "选择项目类型",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "根据项目特点选择合适的开发流程",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProjectTypeCard(
                    title = "A类",
                    description = "全新平台产品开发，完整流程",
                    icon = Icons.Default.Star,
                    color = Color(0xFF6200EE),
                    onClick = { onSelect("A") }
                )
            }
            item {
                ProjectTypeCard(
                    title = "B类",
                    description = "重大改动产品，完整流程",
                    icon = Icons.Default.Whatshot,
                    color = Color(0xFFFF5722),
                    onClick = { onSelect("B") }
                )
            }
            item {
                ProjectTypeCard(
                    title = "C类",
                    description = "小改动产品，标准流程",
                    icon = Icons.Default.Build,
                    color = Color(0xFF4CAF50),
                    onClick = { onSelect("C") }
                )
            }
            item {
                ProjectTypeCard(
                    title = "D类",
                    description = "仅外观变动，极简流程",
                    icon = Icons.Default.Palette,
                    color = Color(0xFF9C27B0),
                    onClick = { onSelect("D") }
                )
            }
        }
    }
}

@Composable
fun ProjectTypeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = color)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun Step2_Options(
    hasMold: Boolean,
    isIoT: Boolean,
    isGEA: Boolean,
    onHasMoldChange: (Boolean) -> Unit,
    onIsIoTChange: (Boolean) -> Unit,
    onIsGEAChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("配置项目选项", style = MaterialTheme.typography.titleLarge)
        Text("根据实际情况选择", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("模具选项", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = hasMold,
                        onClick = { onHasMoldChange(true) },
                        label = { Text("需要开模") }
                    )
                    FilterChip(
                        selected = !hasMold,
                        onClick = { onHasMoldChange(false) },
                        label = { Text("不开模") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("物联选项", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isIoT,
                        onClick = { onIsIoTChange(false) },
                        label = { Text("非物联产品") }
                    )
                    FilterChip(
                        selected = isIoT,
                        onClick = { onIsIoTChange(true) },
                        label = { Text("物联家电") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("GEA项目", style = MaterialTheme.typography.titleSmall)
                        Text("中国市场GE牌产品必选", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isGEA, onCheckedChange = onIsGEAChange)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("下一步", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3_ProjectInfo(
    projectName: String,
    projectDescription: String,
    startDate: Long,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onNext: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = projectName,
            onValueChange = onNameChange,
            label = { Text("项目名称 *") },
            placeholder = { Text("例如：新一代智能穿戴设备") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = projectDescription,
            onValueChange = onDescriptionChange,
            label = { Text("项目描述（选填）") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 开始日期
        Text("计划开始日期", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
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
                    Text(dateFormat.format(Date(startDate)))
                }
                Icon(Icons.Default.Edit, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = projectName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("预览项目计划", style = MaterialTheme.typography.titleMedium)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onStartDateChange(it) }
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
}

@Composable
fun Step4_Confirm(
    projectName: String,
    scheduledTasks: List<ScheduledTask>,
    selectedTasks: List<TaskTemplate>,
    onTaskDurationChange: (Int, Int) -> Unit,
    onCreate: () -> Unit,
    isCreating: Boolean,
    onRecalculate: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        // 项目信息头部
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(projectName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${scheduledTasks.size} 个任务", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRecalculate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重新计算日期")
                }
            }
        }

        // 任务列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(scheduledTasks) { scheduled ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${scheduled.template.id}. ${scheduled.template.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (scheduled.template.deliverable.isNotEmpty()) {
                            Text(
                                "交付: ${scheduled.template.deliverable}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${dateFormat.format(Date(scheduled.plannedStartDate))} - ${dateFormat.format(Date(scheduled.plannedEndDate))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // 工期调整
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("工期:", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        if (scheduled.template.duration > 1) {
                                            onTaskDurationChange(scheduled.template.id, scheduled.template.duration - 1)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "减少", modifier = Modifier.size(16.dp))
                                }
                                Text("${scheduled.template.duration}天", style = MaterialTheme.typography.labelMedium)
                                IconButton(
                                    onClick = {
                                        onTaskDurationChange(scheduled.template.id, scheduled.template.duration + 1)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "增加", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // 创建按钮
        Button(
            onClick = onCreate,
            enabled = !isCreating,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建项目", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

fun shareText(context: Context, content: String, filename: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, filename)
        }
        context.startActivity(Intent.createChooser(intent, "分享项目计划"))
    } catch (e: Exception) {
        Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
