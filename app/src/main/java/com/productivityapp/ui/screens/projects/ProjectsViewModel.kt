package com.productivityapp.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.ProjectStatus
import com.productivityapp.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectsUiState(
    val projects: List<Project> = emptyList(),
    val filteredProjects: List<Project> = emptyList(),
    val selectedFilter: ProjectStatus? = null,
    val isLoading: Boolean = true,
    val showDialog: Boolean = false,
    val editingProject: Project? = null
)

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            projectRepository.getAllProjects()
                .collect { projects ->
                    _uiState.update { state ->
                        state.copy(
                            projects = projects,
                            filteredProjects = filterProjects(projects, state.selectedFilter),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun setFilter(status: ProjectStatus?) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = status,
                filteredProjects = filterProjects(state.projects, status)
            )
        }
    }

    private fun filterProjects(projects: List<Project>, status: ProjectStatus?): List<Project> {
        return if (status == null) projects
        else projects.filter { it.status == status }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showDialog = true, editingProject = null) }
    }

    fun showEditDialog(project: Project) {
        _uiState.update { it.copy(showDialog = true, editingProject = project) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showDialog = false, editingProject = null) }
    }

    fun createProject(title: String, description: String, colorHex: String) {
        viewModelScope.launch {
            val project = Project(
                title = title,
                description = description,
                colorHex = colorHex,
                status = ProjectStatus.ACTIVE
            )
            projectRepository.insertProject(project)
            dismissDialog()
        }
    }

    fun updateProject(project: Project, title: String, description: String, colorHex: String) {
        viewModelScope.launch {
            projectRepository.updateProject(
                project.copy(
                    title = title,
                    description = description,
                    colorHex = colorHex
                )
            )
            dismissDialog()
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            projectRepository.deleteProject(project)
        }
    }
}
