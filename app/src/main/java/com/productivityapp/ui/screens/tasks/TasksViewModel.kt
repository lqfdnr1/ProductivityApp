package com.productivityapp.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Plan
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskStatus
import com.productivityapp.data.repository.PlanRepository
import com.productivityapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasksUiState(
    val plan: Plan? = null,
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planRepository: PlanRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val planId: Long = checkNotNull(savedStateHandle["planId"])

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                planRepository.getPlanById(planId),
                taskRepository.getTasksByPlan(planId)
            ) { plan, tasks ->
                TasksUiState(
                    plan = plan,
                    tasks = tasks.sortedBy { it.order },
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateTaskStatus(task: Task, status: TaskStatus) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(task.id, status)
        }
    }
}
