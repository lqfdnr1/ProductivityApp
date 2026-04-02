package com.productivityapp.di

import android.content.Context
import androidx.room.Room
import com.productivityapp.data.local.database.AppDatabase
import com.productivityapp.data.local.database.PlanDao
import com.productivityapp.data.local.database.ProjectDao
import com.productivityapp.data.local.database.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "productivity_app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    @Singleton
    fun providePlanDao(database: AppDatabase): PlanDao {
        return database.planDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }
}
