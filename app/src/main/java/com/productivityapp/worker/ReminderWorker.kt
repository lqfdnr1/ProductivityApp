package com.productivityapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.productivityapp.MainActivity
import com.productivityapp.R
import com.productivityapp.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1)
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: "Task Reminder"

        if (taskId == -1L) return Result.failure()

        createNotificationChannel()
        showNotification(taskId, taskTitle)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task reminders"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(taskId: Long, taskTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Task Reminder")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(taskId.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"

        fun scheduleReminder(context: Context, taskId: Long, taskTitle: String, delayMillis: Long) {
            val inputData = Data.Builder()
                .putLong(KEY_TASK_ID, taskId)
                .putString(KEY_TASK_TITLE, taskTitle)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_$taskId")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "reminder_$taskId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }

        fun cancelReminder(context: Context, taskId: Long) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("reminder_$taskId")
        }
    }
}
