package com.productivityapp.data.repository

import com.productivityapp.data.local.database.ProjectDao
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.ProjectStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()

    fun getProjectsByStatus(status: ProjectStatus): Flow<List<Project>> =
        projectDao.getProjectsByStatus(status)

    fun getProjectById(id: Long): Flow<Project?> = projectDao.getProjectById(id)

    suspend fun getProjectByIdOnce(id: Long): Project? = projectDao.getProjectByIdOnce(id)

    suspend fun insertProject(project: Project): Long = projectDao.insertProject(project)

    suspend fun updateProject(project: Project) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    suspend fun deleteProjectById(id: Long) = projectDao.deleteProjectById(id)
}
