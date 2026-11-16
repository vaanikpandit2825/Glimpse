
package com.example.glimpse.firebase

import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class FCMTokenRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = GlimpseDatabase.instance

    private fun isDebugBuild(): Boolean {
        val context = FirebaseApp.getInstance().applicationContext

        return (context.applicationInfo.flags and
                ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun registerCurrentDevice(
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrBlank()) {
            onFailure(Exception("No logged-in Firebase user"))
            return
        }

        // Never request an FCM token during local debug testing.
        if (isDebugBuild()) {
            Log.d(
                "GLIMPSE_FCM",
                "Debug mode: FCM token registration skipped"
            )
            onSuccess()
            return
        }

        Log.d(
            "GLIMPSE_FCM",
            "Registering token for UID: $uid"
        )

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->

                Log.d(
                    "GLIMPSE_FCM",
                    "FCM token obtained successfully"
                )

                database.getReference("fcmTokens")
                    .child(uid)
                    .child(token)
                    .setValue(true)
                    .addOnSuccessListener {
                        Log.d(
                            "GLIMPSE_FCM",
                            "Token saved successfully"
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "GLIMPSE_FCM",
                            "Database write failed",
                            exception
                        )
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "GLIMPSE_FCM",
                    "FCM token retrieval failed",
                    exception
                )
                onFailure(exception)
            }
    }

    fun removeCurrentDeviceToken(
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrBlank()) {
            onFailure(
                Exception(
                    "You must log in to remove your notification token."
                )
            )
            return
        }

        // Never request an FCM token during local debug testing.
        if (isDebugBuild()) {
            Log.d(
                "GLIMPSE_FCM",
                "Debug mode: FCM token removal skipped"
            )
            onSuccess()
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                database.getReference("fcmTokens")
                    .child(uid)
                    .child(token)
                    .removeValue()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onFailure(it)
                    }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}
