
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
            onFailure(Exception("You must log in to register for notifications."))
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val tokenRef = database.getReference("fcmTokens")
                    .child(uid)
                    .child(token)

                tokenRef.setValue(true)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
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
