package com.productivityapp.ui.screens.home

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
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val recentProjects: List<Project> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            projectRepository.getAllProjects()
                .combine(getTodayTasks()) { projects, todayTasks ->
                    HomeUiState(
                        recentProjects = projects.take(5),
                        todayTasks = todayTasks,
                        upcomingTasks = todayTasks.filter { it.status != TaskStatus.COMPLETED },
                        isLoading = false
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    private fun getTodayTasks(): Flow<List<Task>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        return taskRepository.getTasksForDay(startOfDay, endOfDay)
    }
}
