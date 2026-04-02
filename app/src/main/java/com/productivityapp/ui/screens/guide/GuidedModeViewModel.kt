package com.productivityapp.ui.screens.guide

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskStatus
import com.productivityapp.data.repository.ProjectRepository
import com.productivityapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuidedModeUiState(
    val project: Project? = null,
    val tasks: List<Task> = emptyList(),
    val currentTaskIndex: Int = 0,
    val completedCount: Int = 0,
    val skippedCount: Int = 0,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false
)

@HiltViewModel
class GuidedModeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    private val _uiState = MutableStateFlow(GuidedModeUiState())
    val uiState: StateFlow<GuidedModeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                projectRepository.getProjectById(projectId),
                taskRepository.getTasksByProject(projectId)
            ) { project, tasks ->
                val pendingTasks = tasks.filter {
                    it.status != TaskStatus.COMPLETED && it.status != TaskStatus.SKIPPED
                }
                GuidedModeUiState(
                    project = project,
                    tasks = pendingTasks,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun completeCurrentTask() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentTask = state.tasks.getOrNull(state.currentTaskIndex) ?: return@launch

            taskRepository.updateTaskStatus(currentTask.id, TaskStatus.COMPLETED)

            _uiState.update {
                it.copy(
                    completedCount = it.completedCount + 1,
                    currentTaskIndex = calculateNextIndex(it.currentTaskIndex + 1)
                )
            }
            checkIfFinished()
        }
    }

    fun skipCurrentTask() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentTask = state.tasks.getOrNull(state.currentTaskIndex) ?: return@launch

            taskRepository.updateTaskStatus(currentTask.id, TaskStatus.SKIPPED)

            _uiState.update {
                it.copy(
                    skippedCount = it.skippedCount + 1,
                    currentTaskIndex = calculateNextIndex(it.currentTaskIndex + 1)
                )
            }
            checkIfFinished()
        }
    }

    private fun calculateNextIndex(fromIndex: Int): Int {
        val state = _uiState.value
        // Find next incomplete task
        for (i in fromIndex until state.tasks.size) {
            val task = state.tasks[i]
            // Task might have been completed/skippe externally
            if (task.status != TaskStatus.COMPLETED && task.status != TaskStatus.SKIPPED) {
                return i
            }
        }
        // Check earlier tasks
        for (i in 0 until minOf(fromIndex, state.tasks.size)) {
            val task = state.tasks[i]
            if (task.status != TaskStatus.COMPLETED && task.status != TaskStatus.SKIPPED) {
                return i
            }
        }
        return state.tasks.size // All done
    }

    private fun checkIfFinished() {
        val state = _uiState.value
        if (state.currentTaskIndex >= state.tasks.size) {
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    fun goToPreviousTask() {
        val state = _uiState.value
        if (state.currentTaskIndex > 0) {
            _uiState.update { it.copy(currentTaskIndex = it.currentTaskIndex - 1) }
        }
    }

    fun getCurrentTask(): Task? {
        val state = _uiState.value
        return state.tasks.getOrNull(state.currentTaskIndex)
    }

    fun getProgress(): Float {
        val state = _uiState.value
        val total = state.tasks.size
        if (total == 0) return 1f
        return (state.completedCount + state.skippedCount).toFloat() / total
    }

    fun getEncouragingMessage(): String {
        val progress = getProgress()
        return when {
            progress >= 1f -> "🎉 Amazing! You've completed all tasks!"
            progress >= 0.75f -> "🔥 Almost there! Keep going!"
            progress >= 0.5f -> "💪 Halfway done! You're doing great!"
            progress >= 0.25f -> "👍 Good progress! Stay focused!"
            else -> "✨ Let's get started! You can do this!"
        }
    }
}
