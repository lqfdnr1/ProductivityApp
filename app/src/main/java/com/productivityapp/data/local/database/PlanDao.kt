package com.productivityapp.data.local.database

import androidx.room.*
import com.productivityapp.data.model.Plan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans WHERE projectId = :projectId ORDER BY `order` ASC")
    fun getPlansByProject(projectId: Long): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :id")
    fun getPlanById(id: Long): Flow<Plan?>

    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlanByIdOnce(id: Long): Plan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: Plan): Long

    @Update
    suspend fun updatePlan(plan: Plan)

    @Delete
    suspend fun deletePlan(plan: Plan)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deletePlanById(id: Long)

    @Query("SELECT COUNT(*) FROM plans WHERE projectId = :projectId")
    suspend fun getPlanCountForProject(projectId: Long): Int
}
