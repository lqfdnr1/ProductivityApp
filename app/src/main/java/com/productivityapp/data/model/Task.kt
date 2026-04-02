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
    indices = [Index("planId"), Index("preTaskId")]
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
    val completedAt: Long? = null,
    // PRD: 前置任务依赖 - 前置任务ID，前置任务100%完成后才可启动
    val preTaskId: Long? = null,
    // PRD: 任务所属环节分类（对应模块3-7）
    val category: TaskCategory = TaskCategory.NEW_PRODUCT
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED, SKIPPED, BLOCKED
}

// PRD 模块3-7 任务分类
enum class TaskCategory(val displayName: String, val description: String) {
    NEW_PRODUCT("新品数据", "新产品开发数据相关工作"),
    MOLD("模具", "模具设计、加工、验收、调试"),
    TEST("试验", "性能试验、可靠性试验、量产测试"),
    DOCUMENT("资料", "技术资料、图纸、工艺文件、说明书"),
    CERTIFICATION("认证", "行业认证、质量认证、安全认证、专利")
}
