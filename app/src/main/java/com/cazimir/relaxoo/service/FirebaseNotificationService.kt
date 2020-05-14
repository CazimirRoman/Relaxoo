package com.cazimir.relaxoo.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.application.MyApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseNotificationService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "NotificationService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data.let { data ->
                if (data["title"] != null && data["body"] != null) {
                    sendNotification(data["title"]!!, data["body"]!!)
                }
            }
        }
    }

    private fun sendNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java)

        // put intent to refetch data if user has application open
        intent.putExtra("refetch", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, MyApplication.NOTIFICATION_CHANNEL)
                .setContentTitle(getString(R.string.app_name))
                .setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}
