package com.productivityapp.data.local.database

import androidx.room.*
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE planId = :planId ORDER BY `order` ASC")
    fun getTasksByPlan(planId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskByIdOnce(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL ORDER BY dueDate ASC")
    fun getTasksWithDueDate(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startOfDay AND :endOfDay ORDER BY priority DESC")
    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE planId IN (SELECT id FROM plans WHERE projectId = :projectId) ORDER BY `order` ASC")
    fun getTasksByProject(projectId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE planId IN (SELECT id FROM plans WHERE projectId = :projectId) AND category = :category ORDER BY `order` ASC")
    fun getTasksByProjectAndCategory(projectId: Long, category: String): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE planId = :planId")
    suspend fun getTaskCountForPlan(planId: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE planId = :planId AND status = :status")
    suspend fun getTaskCountByStatus(planId: Long, status: TaskStatus): Int

    // PRD: 获取前置任务
    @Query("SELECT * FROM tasks WHERE id = :preTaskId")
    suspend fun getPreTask(preTaskId: Long): Task?

    // PRD: 检查前置任务是否已完成
    @Query("SELECT status FROM tasks WHERE id = :preTaskId")
    suspend fun getPreTaskStatus(preTaskId: Long): TaskStatus?

    // PRD: 按类别统计项目任务完成率
    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE planId IN (SELECT id FROM plans WHERE projectId = :projectId) 
        AND category = :category AND status = 'COMPLETED'
    """)
    suspend fun getCompletedTaskCountByCategory(projectId: Long, category: String): Int

    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE planId IN (SELECT id FROM plans WHERE projectId = :projectId) 
        AND category = :category
    """)
    suspend fun getTotalTaskCountByCategory(projectId: Long, category: String): Int

    // PRD: 获取被阻塞的任务（前置任务未完成）
    @Query("SELECT * FROM tasks WHERE preTaskId IS NOT NULL AND preTaskId != 0 AND status = 'PENDING'")
    fun getPotentiallyBlockedTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: TaskStatus, completedAt: Long?)
}
