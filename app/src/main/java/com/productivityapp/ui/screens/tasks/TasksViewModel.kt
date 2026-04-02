package com.productivityapp.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Plan
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskCategory
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
    val filteredTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val selectedCategory: TaskCategory? = null, // PRD: 类别过滤
    val blockedTaskIds: Set<Long> = emptySet()   // PRD: 被阻塞的任务ID
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
                Pair(plan, tasks)
            }.collect { (plan, tasks) ->
                val blockedIds = computeBlockedTaskIds(tasks)
                _uiState.update { state ->
                    state.copy(
                        plan = plan,
                        tasks = tasks.sortedBy { it.order },
                        filteredTasks = filterTasks(tasks, state.selectedCategory),
                        isLoading = false,
                        blockedTaskIds = blockedIds
                    )
                }
            }
        }
    }

    // PRD: 计算被阻塞的任务（前置任务未完成）
    private fun computeBlockedTaskIds(allTasks: List<Task>): Set<Long> {
        val blocked = mutableSetOf<Long>()
        for (task in allTasks) {
            val preTaskId = task.preTaskId
            if (preTaskId != null && preTaskId != 0L) {
                val preTask = allTasks.find { it.id == preTaskId }
                if (preTask != null && preTask.status != TaskStatus.COMPLETED) {
                    blocked.add(task.id)
                }
            }
        }
        return blocked
    }

    // PRD: 按类别过滤
    private fun filterTasks(tasks: List<Task>, category: TaskCategory?): List<Task> {
        return if (category == null) tasks
        else tasks.filter { it.category == category }
    }

    // PRD: 切换类别过滤
    fun setCategory(category: TaskCategory?) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                filteredTasks = filterTasks(state.tasks, category)
            )
        }
    }

    fun updateTaskStatus(task: Task, status: TaskStatus) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(task.id, status)
            // 重新计算阻塞状态
            val updatedTasks = _uiState.value.tasks.map {
                if (it.id == task.id) it.copy(status = status) else it
            }
            _uiState.update { it.copy(blockedTaskIds = computeBlockedTaskIds(updatedTasks)) }
        }
    }
}
