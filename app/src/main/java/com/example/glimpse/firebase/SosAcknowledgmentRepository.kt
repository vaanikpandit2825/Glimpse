
package com.example.glimpse.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions

class SosAcknowledgmentRepository {

    private val auth = FirebaseAuth.getInstance()

    private val functions: FirebaseFunctions =
        GlimpseFunctions.instance

    fun acknowledgeSos(
        senderUid: String,
        eventId: String,
        onSuccess: (String, Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {

        val currentUid = auth.currentUser?.uid

        if (currentUid.isNullOrBlank()) {
            onFailure("Please log in to acknowledge this emergency.")
            return
        }

        val validKey = Regex("[A-Za-z0-9_-]{1,128}")

        if (
            !validKey.matches(senderUid) ||
            !validKey.matches(eventId)
        ) {
            onFailure("Invalid emergency details.")
            return
        }

        if (currentUid == senderUid) {
            onFailure("You cannot acknowledge your own SOS.")
            return
        }

        val request = hashMapOf(
            "senderUid" to senderUid,
            "eventId" to eventId
        )

        functions
            .getHttpsCallable("acknowledgeSos")
            .call(request)
            .addOnSuccessListener { result ->

                if (auth.currentUser?.uid != currentUid) {
                    onFailure("Your account has changed.")
                    return@addOnSuccessListener
                }

                val data = result.data as? Map<*, *>

                val acknowledged =
                    data?.get("acknowledged") as? Boolean ?: false

                val acknowledgedBy =
                    data?.get("acknowledgedBy") as? String

                val firstResponder =
                    data?.get("firstResponder") as? Boolean ?: false

                if (
                    acknowledged &&
                    !acknowledgedBy.isNullOrBlank()
                ) {
                    onSuccess(
                        acknowledgedBy,
                        firstResponder
                    )
                } else {
                    onFailure(
                        "Unable to confirm emergency acknowledgment."
                    )
                }
            }
            .addOnFailureListener { error ->

                onFailure(
                    error.localizedMessage
                        ?: "Unable to acknowledge this emergency. Please try again."
                )
            }
    }
}

