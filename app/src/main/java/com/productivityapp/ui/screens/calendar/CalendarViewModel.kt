package com.productivityapp.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.Task
import com.productivityapp.data.repository.ProjectRepository
import com.productivityapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: Calendar = Calendar.getInstance(),
    val selectedDate: Calendar = Calendar.getInstance(),
    val tasksByDate: Map<Long, List<Task>> = emptyMap(),
    val selectedDateTasks: List<Task> = emptyList(),
    val projects: Map<Long, Project> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            combine(
                taskRepository.getTasksWithDueDate(),
                projectRepository.getAllProjects()
            ) { tasks, projects ->
                val projectsMap = projects.associateBy { it.id }
                val tasksByDate = tasks.groupBy { task ->
                    task.dueDate?.let { date ->
                        Calendar.getInstance().apply {
                            timeInMillis = date
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    }
                }.filterKeys { it != null }.mapKeys { it.key!! }

                _uiState.update { state ->
                    state.copy(
                        tasksByDate = tasksByDate,
                        projects = projectsMap,
                        isLoading = false
                    )
                }
                updateSelectedDateTasks()
            }.collect()
        }
    }

    fun selectDate(date: Calendar) {
        _uiState.update { it.copy(selectedDate = date.clone() as Calendar) }
        updateSelectedDateTasks()
    }

    fun previousMonth() {
        _uiState.update { state ->
            val newMonth = state.currentMonth.clone() as Calendar
            newMonth.add(Calendar.MONTH, -1)
            state.copy(currentMonth = newMonth)
        }
    }

    fun nextMonth() {
        _uiState.update { state ->
            val newMonth = state.currentMonth.clone() as Calendar
            newMonth.add(Calendar.MONTH, 1)
            state.copy(currentMonth = newMonth)
        }
    }

    private fun updateSelectedDateTasks() {
        val state = _uiState.value
        val selectedDateMillis = state.selectedDate.clone() as Calendar
        selectedDateMillis.set(Calendar.HOUR_OF_DAY, 0)
        selectedDateMillis.set(Calendar.MINUTE, 0)
        selectedDateMillis.set(Calendar.SECOND, 0)
        selectedDateMillis.set(Calendar.MILLISECOND, 0)

        val tasks = state.tasksByDate[selectedDateMillis.timeInMillis] ?: emptyList()
        _uiState.update { it.copy(selectedDateTasks = tasks) }
    }

    fun getDaysInMonth(): List<CalendarDay> {
        val state = _uiState.value
        val month = state.currentMonth
        val calendar = month.clone() as Calendar

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<CalendarDay>()

        // Add empty days for alignment
        repeat(firstDayOfWeek) {
            days.add(CalendarDay(isEmpty = true))
        }

        // Add days of month
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val dateMillis = calendar.timeInMillis
            val hasTasks = state.tasksByDate.containsKey(dateMillis)
            val isToday = isSameDay(calendar, Calendar.getInstance())
            val isSelected = isSameDay(calendar, state.selectedDate)
            val tasks = state.tasksByDate[dateMillis] ?: emptyList()

            days.add(
                CalendarDay(
                    dayOfMonth = day,
                    dateMillis = dateMillis,
                    hasTasks = hasTasks,
                    isToday = isToday,
                    isSelected = isSelected,
                    tasks = tasks
                )
            )
        }

        return days
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
}

data class CalendarDay(
    val dayOfMonth: Int = 0,
    val dateMillis: Long = 0,
    val hasTasks: Boolean = false,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isEmpty: Boolean = false,
    val tasks: List<Task> = emptyList()
)
