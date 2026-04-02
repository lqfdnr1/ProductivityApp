package com.productivityapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val colorHex: String = "#6200EE",
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ProjectStatus {
    ACTIVE, COMPLETED, ARCHIVED
}
