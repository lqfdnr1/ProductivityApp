package com.productivityapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.productivityapp.data.model.Plan
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.Task

@Database(
    entities = [Project::class, Plan::class, Task::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun planDao(): PlanDao
    abstract fun taskDao(): TaskDao
}
