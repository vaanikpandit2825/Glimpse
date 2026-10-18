package com.example.glimpse.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.glimpse.MainActivity
import com.example.glimpse.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class GlimpseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val uid = com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: return

        val database = com.google.firebase.database.FirebaseDatabase
            .getInstance(
                "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"
            )

        database.getReference("fcmTokens")
            .child(uid)
            .child(token)
            .setValue(true)
    }



    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        android.util.Log.d(
            "GLIMPSE_FCM",
            "Received data keys: ${message.data.keys}"
        )

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "GLIMPSE SOS"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Someone might need your help."

        val senderUid = message.data["senderUid"]
        val eventId = message.data["eventId"]

        showNotification(
            title = title,
            body = body,
            senderUid = senderUid,
            eventId = eventId
        )
    }



    private fun showNotification(
        title: String,
        body: String,
        senderUid: String?,
        eventId: String?
    ) {
        val channelId = "glimpse_sos_alerts"

        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency SOS notifications"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            if (!senderUid.isNullOrBlank() && !eventId.isNullOrBlank()) {
                putExtra("senderUid", senderUid)
                putExtra("eventId", eventId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (eventId ?: System.currentTimeMillis().toString()).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(
                (eventId ?: System.currentTimeMillis().toString()).hashCode(),
                notification
            )
        }
    }

}