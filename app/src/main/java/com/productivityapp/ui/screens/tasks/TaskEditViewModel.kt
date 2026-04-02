package com.productivityapp.ui.screens.tasks

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskPriority
import com.productivityapp.data.model.TaskStatus
import com.productivityapp.data.repository.TaskRepository
import com.productivityapp.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class TaskEditUiState(
    val task: Task? = null,
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val taskId: Long = savedStateHandle["taskId"] ?: -1L
    private val planId: Long = checkNotNull(savedStateHandle["planId"])

    private val _uiState = MutableStateFlow(TaskEditUiState())
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    init {
        if (taskId != -1L) {
            loadTask()
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            taskRepository.getTaskByIdOnce(taskId)?.let { task ->
                _uiState.update {
                    it.copy(
                        task = task,
                        title = task.title,
                        description = task.description,
                        dueDate = task.dueDate,
                        priority = task.priority,
                        status = task.status,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateDueDate(date: Long?) {
        _uiState.update { it.copy(dueDate = date) }
    }

    fun updatePriority(priority: TaskPriority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun saveTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            val task = Task(
                id = state.task?.id ?: 0L,
                planId = planId,
                title = state.title,
                description = state.description,
                dueDate = state.dueDate,
                priority = state.priority,
                status = state.status,
                order = state.task?.order ?: 0,
                createdAt = state.task?.createdAt ?: System.currentTimeMillis()
            )

            val savedId = if (task.id == 0L) {
                taskRepository.insertTask(task)
            } else {
                taskRepository.updateTask(task)
                task.id
            }

            // Schedule reminder if due date is set
            state.dueDate?.let { dueDate ->
                val delay = dueDate - System.currentTimeMillis()
                if (delay > 0) {
                    ReminderWorker.scheduleReminder(context, savedId, state.title, delay)
                }
            }

            _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            _uiState.value.task?.let { task ->
                ReminderWorker.cancelReminder(context, task.id)
                taskRepository.deleteTask(task)
                _uiState.update { it.copy(savedSuccessfully = true) }
            }
        }
    }
}
