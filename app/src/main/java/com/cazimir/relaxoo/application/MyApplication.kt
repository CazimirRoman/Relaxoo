package com.cazimir.relaxoo.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.cazimir.utilitieslibrary.SharedPreferencesUtil

class MyApplication : Application() {

    companion object {
        const val FOREGROUND_SERVICE_CHANNEL = "FOREGROUND_SERVICE_CHANNEL"
        const val NOTIFICATION_CHANNEL = "NOTIFICATION_CHANNEL"
    }

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesUtil.with(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundServiceChannel = NotificationChannel(
                    FOREGROUND_SERVICE_CHANNEL,
                    "Foreground service channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    "Notification channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )

            foregroundServiceChannel.importance = NotificationManager.IMPORTANCE_LOW
            notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(notificationChannel, foregroundServiceChannel))
        }
    }
}
