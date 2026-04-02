package com.productivityapp.ui.screens.projects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Plan
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.ProjectStatus
import com.productivityapp.data.model.Task
import com.productivityapp.data.repository.PlanRepository
import com.productivityapp.data.repository.ProjectRepository
import com.productivityapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectDetailUiState(
    val project: Project? = null,
    val plans: List<Plan> = emptyList(),
    val tasksByPlan: Map<Long, List<Task>> = emptyMap(),
    val isLoading: Boolean = true,
    val showPlanDialog: Boolean = false,
    val editingPlan: Plan? = null
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val planRepository: PlanRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                projectRepository.getProjectById(projectId),
                planRepository.getPlansByProject(projectId)
            ) { project, plans ->
                Pair(project, plans)
            }.collect { (project, plans) ->
                _uiState.update {
                    it.copy(
                        project = project,
                        plans = plans,
                        isLoading = false
                    )
                }
                plans.forEach { plan ->
                    launch {
                        taskRepository.getTasksByPlan(plan.id).collect { tasks ->
                            _uiState.update { state ->
                                state.copy(
                                    tasksByPlan = state.tasksByPlan + (plan.id to tasks)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun showCreatePlanDialog() {
        _uiState.update { it.copy(showPlanDialog = true, editingPlan = null) }
    }

    fun showEditPlanDialog(plan: Plan) {
        _uiState.update { it.copy(showPlanDialog = true, editingPlan = plan) }
    }

    fun dismissPlanDialog() {
        _uiState.update { it.copy(showPlanDialog = false, editingPlan = null) }
    }

    fun createPlan(title: String, description: String) {
        viewModelScope.launch {
            val plan = Plan(
                projectId = projectId,
                title = title,
                description = description,
                order = _uiState.value.plans.size
            )
            planRepository.insertPlan(plan)
            dismissPlanDialog()
        }
    }

    fun updatePlan(plan: Plan, title: String, description: String) {
        viewModelScope.launch {
            planRepository.updatePlan(plan.copy(title = title, description = description))
            dismissPlanDialog()
        }
    }

    fun deletePlan(plan: Plan) {
        viewModelScope.launch {
            planRepository.deletePlan(plan)
        }
    }

    fun updateProjectStatus(status: ProjectStatus) {
        viewModelScope.launch {
            _uiState.value.project?.let { project ->
                projectRepository.updateProject(project.copy(status = status))
            }
        }
    }
}
