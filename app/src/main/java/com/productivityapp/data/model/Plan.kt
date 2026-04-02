package com.productivityapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plans",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class Plan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val title: String,
    val description: String = "",
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
