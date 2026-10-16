
package com.example.glimpse.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class FCMTokenRepository {

    private val auth = FirebaseAuth.getInstance()

    private val database = FirebaseDatabase.getInstance(
        "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"
    )


    fun registerCurrentDevice(
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrBlank()) {
            onFailure(Exception("No logged-in Firebase user"))
            return
        }

        android.util.Log.d("GLIMPSE_FCM", "Registering token for UID: $uid")

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                android.util.Log.d(
                    "GLIMPSE_FCM",
                    "FCM token obtained successfully"
                )

                database.getReference("fcmTokens")
                    .child(uid)
                    .child(token)
                    .setValue(true)
                    .addOnSuccessListener {
                        android.util.Log.d(
                            "GLIMPSE_FCM",
                            "Token saved successfully"
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        android.util.Log.e(
                            "GLIMPSE_FCM",
                            "Database write failed",
                            exception
                        )
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                android.util.Log.e(
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
            onFailure(Exception("You must log in to remove your notification token."))
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                database.getReference("fcmTokens")
                    .child(uid)
                    .child(token)
                    .removeValue()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
