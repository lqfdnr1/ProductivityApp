package com.productivityapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.ProjectStatus
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskCategory
import com.productivityapp.data.model.TaskStatus
import com.productivityapp.data.repository.PlanRepository
import com.productivityapp.data.repository.ProjectRepository
import com.productivityapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val activeProjects: List<Project> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    // PRD: 项目进度 map
    val projectProgress: Map<Long, Float> = emptyMap(),
    // PRD: 环节统计数据
    val categoryStats: Map<TaskCategory, Pair<Int, Int>> = emptyMap(), // category -> (completed, total)
    // PRD: planId -> 项目名称
    val planProjectNames: Map<Long, String> = emptyMap()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val planRepository: PlanRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 1. 获取进行中的项目
            projectRepository.getAllProjects()
                .combine(getTodayTasks()) { projects, todayTasks ->
                    val active = projects.filter { it.status == ProjectStatus.ACTIVE }
                    Triple(active, todayTasks, active.associate { it.id to it.title })
                }
                .collect { (activeProjects, todayTasks, projectNames) ->
                    // 获取 planId -> projectName 的映射
                    val planProjectNames = mutableMapOf<Long, String>()
                    planRepository.getPlansByProject(activeProjects.map { it.id }.firstOrNull() ?: 0).first().forEach { plan ->
                        val proj = activeProjects.find { it.id == plan.projectId }
                        if (proj != null) {
                            planProjectNames[plan.id] = proj.title
                        }
                    }

                    // 计算各环节统计数据
                    val stats = TaskCategory.entries.associateWith { category ->
                        val catTasks = todayTasks.filter { it.category == category }
                        val completed = catTasks.count { it.status == TaskStatus.COMPLETED }
                        completed to catTasks.size
                    }

                    _uiState.update {
                        it.copy(
                            activeProjects = activeProjects,
                            todayTasks = todayTasks,
                            projectProgress = computeProjectProgress(activeProjects),
                            categoryStats = stats,
                            planProjectNames = planProjectNames,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun computeProjectProgress(projects: List<Project>): Map<Long, Float> {
        return projects.associate { it.id to 0f }
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
