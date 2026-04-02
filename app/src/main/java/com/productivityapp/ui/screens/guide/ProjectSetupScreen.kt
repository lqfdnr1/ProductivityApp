package com.productivityapp.ui.screens.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSetupScreen(
    onNavigateBack: () -> Unit,
    onProjectCreated: (Long) -> Unit,
    viewModel: ProjectSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(0) } // 0: 选择类型, 1: 填写信息

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
                .padding(16.dp)
        ) {
            // 步骤指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StepIndicator(
                    step = 1,
                    label = "选择类型",
                    isActive = currentStep == 0,
                    isCompleted = currentStep > 0
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .align(Alignment.CenterVertically)
                        .background(
                            if (currentStep > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
                StepIndicator(
                    step = 2,
                    label = "填写信息",
                    isActive = currentStep == 1,
                    isCompleted = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (currentStep) {
                0 -> ProjectTypeSelection(
                    projectTypes = uiState.projectTypes,
                    selectedType = uiState.selectedType,
                    onTypeSelected = { viewModel.selectProjectType(it) },
                    onNext = { if (uiState.selectedType != null) currentStep = 1 }
                )
                1 -> ProjectInfoForm(
                    projectName = uiState.projectName,
                    projectDescription = uiState.projectDescription,
                    selectedType = uiState.selectedType,
                    phases = uiState.phases,
                    onNameChange = { viewModel.updateProjectName(it) },
                    onDescriptionChange = { viewModel.updateProjectDescription(it) },
                    onBack = { currentStep = 0 },
                    onCreate = { viewModel.createProjectWithWorkflow() },
                    isCreating = uiState.isCreating
                )
            }
        }
    }
}

@Composable
fun StepIndicator(
    step: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProjectTypeSelection(
    projectTypes: List<ProjectType>,
    selectedType: ProjectType?,
    onTypeSelected: (ProjectType) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "选择项目类型",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "根据你的项目特点选择合适的开发流程",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projectTypes) { type ->
                ProjectTypeCard(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelected(type) }
                )
            }
        }

        Button(
            onClick = onNext,
            enabled = selectedType != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("下一步", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ProjectTypeCard(
    type: ProjectType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProjectInfoForm(
    projectName: String,
    projectDescription: String,
    selectedType: ProjectType?,
    phases: List<DevPhase>,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    isCreating: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = projectName,
            onValueChange = onNameChange,
            label = { Text("项目名称") },
            placeholder = { Text("例如：新一代智能穿戴设备") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = projectDescription,
            onValueChange = onDescriptionChange,
            label = { Text("项目描述（选填）") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 显示将要创建的计划
        Text(
            text = "将自动创建以下计划：",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            LazyColumn(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(phases) { phase ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = phase.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${phase.tasks.size}个任务)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("上一步")
            }
            Button(
                onClick = onCreate,
                enabled = projectName.isNotBlank() && !isCreating,
                modifier = Modifier.weight(1f)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("创建项目")
                }
            }
        }
    }
}
