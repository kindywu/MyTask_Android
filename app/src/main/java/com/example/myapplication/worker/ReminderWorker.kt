package com.example.myapplication.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.R
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.db.entity.TaskEntity

class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val tasks = db.taskDao().getAllSnapshot()

        val now = System.currentTimeMillis()
        for (task in tasks) {
            val reminderMin = task.reminderMinutes ?: continue
            val dueDate = task.dueDate ?: continue
            val reminderTime = dueDate - (reminderMin * 60_000L)

            if (reminderTime <= now && reminderTime > now - 5 * 60_000L) {
                sendNotification(task)
            }
        }
        return Result.success()
    }

    private fun sendNotification(task: TaskEntity) {
        val intent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName) ?: return
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, task.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(task.title)
            .setContentText(task.notes.ifBlank { "Task reminder" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID_BASE + task.id.toInt(), notification)
        }
    }
}
