package com.productivityapp.data.repository

import com.productivityapp.data.local.database.PlanDao
import com.productivityapp.data.model.Plan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepository @Inject constructor(
    private val planDao: PlanDao
) {
    fun getPlansByProject(projectId: Long): Flow<List<Plan>> =
        planDao.getPlansByProject(projectId)

    fun getPlanById(id: Long): Flow<Plan?> = planDao.getPlanById(id)

    suspend fun getPlanByIdOnce(id: Long): Plan? = planDao.getPlanByIdOnce(id)

    suspend fun insertPlan(plan: Plan): Long = planDao.insertPlan(plan)

    suspend fun updatePlan(plan: Plan) = planDao.updatePlan(plan)

    suspend fun deletePlan(plan: Plan) = planDao.deletePlan(plan)

    suspend fun deletePlanById(id: Long) = planDao.deletePlanById(id)

    suspend fun getPlanCountForProject(projectId: Long): Int =
        planDao.getPlanCountForProject(projectId)
}
