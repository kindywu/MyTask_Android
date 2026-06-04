package com.example.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.myapplication.worker.ReminderWorker

class MyApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_ID,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH,
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
