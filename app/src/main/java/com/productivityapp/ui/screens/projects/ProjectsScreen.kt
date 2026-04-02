package com.productivityapp.ui.screens.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.ProjectStatus
import com.productivityapp.ui.components.ProjectCard
import com.productivityapp.ui.theme.ProjectColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onNavigateToProject: (Long) -> Unit,
    onNavigateToProjectSetup: () -> Unit,
    onNavigateToTemplateManagement: () -> Unit,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("项目") },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("模板管理") },
                            onClick = {
                                showMenu = false
                                onNavigateToTemplateManagement()
                            },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToProjectSetup,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "创建项目")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No projects found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredProjects, key = { it.id }) { project ->
                        Box {
                            ProjectCard(
                                project = project,
                                onClick = { onNavigateToProject(project.id) },
                                onMenuClick = { showMenu = project.id }
                            )

                            DropdownMenu(
                                expanded = showMenu == project.id,
                                onDismissRequest = { showMenu = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showMenu = null
                                        viewModel.showEditDialog(project)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showMenu = null
                                        viewModel.deleteProject(project)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showDialog) {
            ProjectDialog(
                project = uiState.editingProject,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { title, description, colorHex ->
                    if (uiState.editingProject != null) {
                        viewModel.updateProject(uiState.editingProject!!, title, description, colorHex)
                    } else {
                        viewModel.createProject(title, description, colorHex)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: ProjectStatus?,
    onFilterSelected: (ProjectStatus?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
            label = { Text("全部") }
        )
        FilterChip(
            selected = selectedFilter == ProjectStatus.ACTIVE,
            onClick = { onFilterSelected(ProjectStatus.ACTIVE) },
            label = { Text("进行中") }
        )
        FilterChip(
            selected = selectedFilter == ProjectStatus.COMPLETED,
            onClick = { onFilterSelected(ProjectStatus.COMPLETED) },
            label = { Text("已完成") }
        )
        FilterChip(
            selected = selectedFilter == ProjectStatus.ARCHIVED,
            onClick = { onFilterSelected(ProjectStatus.ARCHIVED) },
            label = { Text("已归档") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDialog(
    project: Project?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(project?.title ?: "") }
    var description by remember { mutableStateOf(project?.description ?: "") }
    var selectedColorIndex by remember {
        mutableIntStateOf(
            ProjectColors.indexOfFirst {
                it.toHexString() == project?.colorHex
            }.takeIf { it >= 0 } ?: 0
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (project == null) "Create Project" else "Edit Project") },
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
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProjectColors.take(6).forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (index == selectedColorIndex)
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColorIndex = index }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectColors.drop(6).take(6).forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (index + 6 == selectedColorIndex)
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColorIndex = index + 6 }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, description, ProjectColors[selectedColorIndex].toHexString()) },
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

private fun Color.toHexString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}
