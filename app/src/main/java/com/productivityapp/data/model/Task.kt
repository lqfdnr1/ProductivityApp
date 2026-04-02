package com.productivityapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,
    val title: String,
    val description: String = "",
    val dueDate: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED, SKIPPED
}
