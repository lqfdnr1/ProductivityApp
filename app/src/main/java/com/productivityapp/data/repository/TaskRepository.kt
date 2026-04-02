package com.productivityapp.data.repository

import com.productivityapp.data.local.database.TaskDao
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getTasksByPlan(planId: Long): Flow<List<Task>> = taskDao.getTasksByPlan(planId)

    fun getTaskById(id: Long): Flow<Task?> = taskDao.getTaskById(id)

    suspend fun getTaskByIdOnce(id: Long): Task? = taskDao.getTaskByIdOnce(id)

    fun getTasksWithDueDate(): Flow<List<Task>> = taskDao.getTasksWithDueDate()

    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<Task>> =
        taskDao.getTasksForDay(startOfDay, endOfDay)

    fun getTasksByProject(projectId: Long): Flow<List<Task>> =
        taskDao.getTasksByProject(projectId)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun updateTaskStatus(id: Long, status: TaskStatus) {
        val completedAt = if (status == TaskStatus.COMPLETED) System.currentTimeMillis() else null
        taskDao.updateTaskStatus(id, status, completedAt)
    }

    suspend fun getTaskCountForPlan(planId: Long): Int = taskDao.getTaskCountForPlan(planId)

    suspend fun getTaskCountByStatus(planId: Long, status: TaskStatus): Int =
        taskDao.getTaskCountByStatus(planId, status)
}
