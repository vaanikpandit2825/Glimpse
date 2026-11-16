package com.example.glimpse.firebase

import com.example.glimpse.model.SOSEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

class SOSRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = 
GlimpseDatabase.instance
) {
    private val locationRef = database.getReference("locations")
    private val connectionRef = database.getReference("connections")
    private val sosEventsRef = database.getReference("sosEvents")

    companion object {
        private const val STATUS_ACTIVE = "active"
        private const val STATUS_CANCELLED = "cancelled"
        private const val STATUS_RESOLVED = "resolved"
        private const val MAX_LOCATION_AGE_MS = 2 * 60 * 1000L
    }

    fun createSosevents(
        onSuccess: (SOSEvent) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrBlank()) {
            onFailure(Exception("You must log in to create an SOS event."))
            return
        }

        val eventRef = sosEventsRef.child(uid).push()
        val eventId = eventRef.key

        if (eventId.isNullOrBlank()) {
            onFailure(Exception("Could not generate an SOS event ID."))
            return
        }

        locationRef.child(uid).get()
            .addOnSuccessListener { locationSnapshot ->

                database.getReference("emergencyContacts")
                    .child(uid)
                    .get()
                    .addOnSuccessListener contactsLoaded@{ contactsSnapshot ->
                        try {
                            val recipients = mutableMapOf<String, Boolean>()

                            contactsSnapshot.children.forEach { contact ->
                                val contactUid = contact.key
                                val status = contact
                                    .child("status")
                                    .getValue(String::class.java)

                                if (
                                    !contactUid.isNullOrBlank() &&
                                    contactUid != uid &&
                                    status == "accepted"
                                ) {
                                    recipients[contactUid] = true
                                }
                            }

                            if (recipients.isEmpty()) {
                                onFailure(
                                    IllegalStateException(
                                        "No accepted emergency contacts found."
                                    )
                                )
                                return@contactsLoaded
                            }


                            val now = System.currentTimeMillis()

                            val rawLatitude = locationSnapshot
                                .child("latitude")
                                .getValue(Double::class.java)

                            val rawLongitude = locationSnapshot
                                .child("longitude")
                                .getValue(Double::class.java)

                            val rawTimestamp = locationSnapshot
                                .child("timestamp")
                                .getValue(Long::class.java)

                            val validCoordinates =
                                rawLatitude != null &&
                                        rawLongitude != null &&
                                        rawLatitude in -90.0..90.0 &&
                                        rawLongitude in -180.0..180.0

                            val freshLocation =
                                validCoordinates &&
                                        rawTimestamp != null &&
                                        rawTimestamp > 0L &&
                                        rawTimestamp <= now &&
                                        now - rawTimestamp <= MAX_LOCATION_AGE_MS

                            val event = SOSEvent(
                                id = eventId,
                                userUid = uid,
                                createdAt = now,
                                updatedAt = now,
                                status = STATUS_ACTIVE,
                                latitude = if (freshLocation) rawLatitude else null,
                                longitude = if (freshLocation) rawLongitude else null,
                                locationTimestamp = if (freshLocation) {
                                    rawTimestamp
                                } else {
                                    null
                                },
                                recipientUids = recipients
                            )
                            val record = mapOf(
                                "id" to event.id,
                                "userUid" to event.userUid,
                                "createdAt" to event.createdAt,
                                "updatedAt" to event.updatedAt,
                                "status" to event.status,
                                "latitude" to event.latitude,
                                "longitude" to event.longitude,
                                "locationTimestamp" to event.locationTimestamp,
                                "recipientUids" to event.recipientUids
                            )

                            eventRef.setValue(record)
                                .addOnSuccessListener {
                                    onSuccess(event)
                                }
                                .addOnFailureListener { error ->
                                    onFailure(error)
                                }
                        } catch (error: Exception) {
                            onFailure(error)
                        }
                    }
                    .addOnFailureListener { error ->
                        onFailure(error)
                    }
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
    fun getSosEvent(
        senderUid: String,
        eventId: String,
        onSuccess: (SOSEvent) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val recipientUid = auth.currentUser?.uid

        if (recipientUid.isNullOrBlank()) {
            onFailure(Exception("You must log in to view this SOS event."))
            return
        }

        if (senderUid.isBlank() || eventId.isBlank()) {
            onFailure(Exception("Sender UID and SOS event ID are required."))
            return
        }
        sosEventsRef.child(senderUid).child(eventId).get()
            .addOnSuccessListener { snapshot ->
                val event = snapshot.getValue(SOSEvent::class.java)

                when {
                    event == null ->
                        onFailure(Exception("The SOS event could not be found."))

                    event.userUid != senderUid ->
                        onFailure(Exception("SOS sender verification failed."))

                    recipientUid != senderUid &&
                            event.recipientUids[recipientUid] != true ->
                        onFailure(Exception("You are not an intended recipient of this SOS."))

                    else -> onSuccess(event)
                }
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
    fun cancelSosEvent(
        eventId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateActiveEventStatus(
            eventId = eventId,
            newStatus = STATUS_CANCELLED,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
    fun resolveSosEvent(
        eventId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateActiveEventStatus(
            eventId = eventId,
            newStatus = STATUS_RESOLVED,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
    private fun updateActiveEventStatus(
        eventId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrBlank()) {
            onFailure(Exception("You must be logged in."))
            return
        }
        if (eventId.isBlank()) {
            onFailure(Exception("SOS event ID cannot be empty."))
            return
        }
        if (newStatus != STATUS_CANCELLED &&
            newStatus != STATUS_RESOLVED
        ) {
            onFailure(Exception("Invalid SOS status."))
            return
        }
        val eventRef = sosEventsRef
            .child(uid)
            .child(eventId)
        eventRef.get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) {
                    onFailure(Exception("SOS event not found."))
                    return@addOnSuccessListener
                }
                val currentStatus = snapshot
                    .child("status")
                    .getValue(String::class.java)

                if (currentStatus != STATUS_ACTIVE) {
                    onFailure(
                        Exception("SOS event is no longer active.")
                    )
                    return@addOnSuccessListener
                }
                val updates = mapOf<String, Any?>(
                    "status" to newStatus,
                    "updatedAt" to
                            com.google.firebase.database
                                .ServerValue.TIMESTAMP
                )

                eventRef.updateChildren(updates)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { error ->
                        onFailure(error)
                    }
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

}
