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

    fun getTasksByProjectAndCategory(projectId: Long, category: String): Flow<List<Task>> =
        taskDao.getTasksByProjectAndCategory(projectId, category)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun insertTasks(tasks: List<Task>) = taskDao.insertTasks(tasks)

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

    // PRD: 前置任务依赖检查 - 判断任务是否可以启动
    suspend fun canStartTask(taskId: Long): Boolean {
        val task = taskDao.getTaskByIdOnce(taskId) ?: return false
        val preTaskId = task.preTaskId ?: return true  // 无前置任务，可直接启动
        val preTaskStatus = taskDao.getPreTaskStatus(preTaskId) ?: return true
        return preTaskStatus == TaskStatus.COMPLETED
    }

    // PRD: 获取任务完成率（按类别）
    suspend fun getCategoryCompletionRate(projectId: Long, category: String): Float {
        val total = taskDao.getTotalTaskCountByCategory(projectId, category)
        if (total == 0) return 0f
        val completed = taskDao.getCompletedTaskCountByCategory(projectId, category)
        return completed.toFloat() / total
    }

    // PRD: 批量检查并更新阻塞任务状态
    suspend fun updateBlockedTaskStatuses(projectId: Long) {
        // This would be called to refresh blocked statuses
        // Implementation depends on having the full task list
    }
}
