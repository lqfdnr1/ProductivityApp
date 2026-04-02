package com.productivityapp.ui.screens.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.local.datastore.PreferencesManager
import com.productivityapp.ui.screens.guide.TaskTemplate
import com.productivityapp.ui.screens.guide.TaskTemplateLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class TemplateUiState(
    val allTasks: List<TaskTemplate> = TaskTemplateLibrary.allTasks,
    val customDurations: Map<Int, Int> = emptyMap(),
    val customPrerequisites: Map<Int, List<Int>> = emptyMap(),
    val selectedTask: TaskTemplate? = null,
    val isEditing: Boolean = false
)

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateUiState())
    val uiState: StateFlow<TemplateUiState> = _uiState.asStateFlow()

    init {
        loadCustomizations()
    }

    private fun loadCustomizations() {
        viewModelScope.launch {
            // Load from preferences or use defaults
            _uiState.update { it.copy(customDurations = emptyMap(), customPrerequisites = emptyMap()) }
        }
    }

    fun selectTask(task: TaskTemplate) {
        _uiState.update { it.copy(selectedTask = task, isEditing = true) }
    }

    fun dismissEditor() {
        _uiState.update { it.copy(selectedTask = null, isEditing = false) }
    }

    fun updateTaskDuration(taskId: Int, duration: Int) {
        _uiState.update { state ->
            val newDurations = state.customDurations.toMutableMap()
            newDurations[taskId] = duration
            
            // Update the task in allTasks list
            val updatedTasks = state.allTasks.map { task ->
                if (task.id == taskId) task.copy(duration = duration) else task
            }
            
            state.copy(
                customDurations = newDurations,
                allTasks = updatedTasks,
                selectedTask = state.selectedTask?.copy(duration = duration)
            )
        }
    }

    fun updateTaskPrerequisites(taskId: Int, prerequisites: List<Int>) {
        _uiState.update { state ->
            val newPrereqs = state.customPrerequisites.toMutableMap()
            newPrereqs[taskId] = prerequisites
            
            // Update the task in allTasks list
            val updatedTasks = state.allTasks.map { task ->
                if (task.id == taskId) task.copy(prerequisites = prerequisites) else task
            }
            
            state.copy(
                customPrerequisites = newPrereqs,
                allTasks = updatedTasks,
                selectedTask = state.selectedTask?.copy(prerequisites = prerequisites)
            )
        }
    }

    fun addPrerequisite(taskId: Int, prereqId: Int) {
        val task = _uiState.value.allTasks.find { it.id == taskId } ?: return
        if (prereqId !in task.prerequisites && prereqId != taskId) {
            updateTaskPrerequisites(taskId, task.prerequisites + prereqId)
        }
    }

    fun removePrerequisite(taskId: Int, prereqId: Int) {
        val task = _uiState.value.allTasks.find { it.id == taskId } ?: return
        updateTaskPrerequisites(taskId, task.prerequisites - prereqId)
    }

    fun resetToDefault(taskId: Int) {
        val defaultTask = TaskTemplateLibrary.allTasks.find { it.id == taskId } ?: return
        _uiState.update { state ->
            val newDurations = state.customDurations.toMutableMap()
            newDurations.remove(taskId)
            
            val newPrereqs = state.customPrerequisites.toMutableMap()
            newPrereqs.remove(taskId)
            
            val updatedTasks = state.allTasks.map { task ->
                if (task.id == taskId) defaultTask else task
            }
            
            state.copy(
                customDurations = newDurations,
                customPrerequisites = newPrereqs,
                allTasks = updatedTasks,
                selectedTask = if (state.selectedTask?.id == taskId) defaultTask else state.selectedTask
            )
        }
    }

    fun resetAllToDefault() {
        _uiState.update { 
            it.copy(
                customDurations = emptyMap(),
                customPrerequisites = emptyMap(),
                allTasks = TaskTemplateLibrary.allTasks,
                selectedTask = null
            )
        }
    }
}
