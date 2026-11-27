
package com.example.glimpse.firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.glimpse.MainActivity
import com.example.glimpse.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GlimpseMessagingService : FirebaseMessagingService() {

    private fun isDebugBuild(): Boolean {
        return (applicationInfo.flags and
                ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Firebase Cloud Messaging has no local emulator.
        // Never register an FCM token during debug testing.
        if (isDebugBuild()) {
            Log.d("GLIMPSE_FCM", "Debug: token registration skipped")
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        GlimpseDatabase.instance
            .getReference("fcmTokens")
            .child(uid)
            .child(token)
            .setValue(true)
            .addOnFailureListener { exception ->
                Log.e("GLIMPSE_FCM", "Token registration failed", exception)
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Ignore real FCM messages in local debug builds.
        if (isDebugBuild()) {
            Log.d("GLIMPSE_FCM", "Debug: remote FCM message ignored")
            return
        }

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

        val notificationId =
            (eventId ?: System.currentTimeMillis().toString()).hashCode()

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(notificationId, notification)
        }
    }
}
