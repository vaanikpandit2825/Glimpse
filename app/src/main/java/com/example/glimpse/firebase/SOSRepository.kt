package com.example.glimpse.firebase

import coil.request.SuccessResult
import com.example.glimpse.model.SOSEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.rpc.QuotaFailure

class SOSRepository(
    private val auth: FirebaseAuth= FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase=FirebaseDatabase.getInstance(
        "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
){
    private val locationRef= database.getReference("locations")
    private val connectionRef=database.getReference("connections")
    private val sosEventsRef=database.getReference("sosEvents")

    fun createSosevents(
        onSuccess:(SOSEvent)->Unit,
        onFailure:(Exception)->Unit
    ){
        val uid=auth.currentUser?.uid

        if(uid.isNullOrBlank()){
            onFailure(Exception("You must login to create an SOS event."))
            return
        }
        val eventRef=sosEventsRef.child(uid).push()
        val eventId=eventRef.key

        if(eventId.isNullOrBlank()){
            onFailure(Exception("Could not generate an SOS event ID"))
            return
        }
        locationRef.child(uid).get()
            .addOnCompleteListener {locationTask->
                 val locationSnapshot=
                     if(locationTask.isSuccessful)locationTask.result else null

                connectionRef.child(uid).get()
                    .addOnCompleteListener { connectionTask ->
                        val connectionUids=mutableMapOf<String,Boolean>()

                        if(connectionTask.isSuccessful){
                            connectionTask.result.children.forEach { connection->
                                val partnerUid=connection.key
                                val status=connection.child("status")
                                    .getValue(String::class.java)

                                if(!partnerUid.isNullOrBlank() && status=="connected"){
                                    connectionUids[partnerUid]=true
                                }
                            }
                        }
                        val now = System.currentTimeMillis()

                        val latitude = locationSnapshot
                            ?.child("latitude")
                            ?.getValue(Double::class.java)

                        val longitude = locationSnapshot
                            ?.child("longitude")
                            ?.getValue(Double::class.java)

                        val locationTimestamp = locationSnapshot
                            ?.child("timestamp")
                            ?.getValue(Long::class.java)

                        val event = SOSEvent(
                            id = eventId,
                            userUid = uid,
                            status = "active",
                            createdAt = now,
                            updatedAt = now,
                            latitude = latitude,
                            longitude = longitude,
                            locationTimestamp = locationTimestamp,
                            recipientUids = connectionUids
                        )

                        val record = mutableMapOf<String, Any>(
                            "id" to event.id,
                            "userUid" to event.userUid,
                            "status" to event.status,
                            "createdAt" to event.createdAt,
                            "updatedAt" to event.updatedAt,
                            "recipientUids" to event.recipientUids
                        )

                        if (latitude != null && longitude != null) {
                            record["location"] = mapOf(
                                "latitude" to latitude,
                                "longitude" to longitude,
                                "timestamp" to (locationTimestamp ?: 0L)
                            )
                        }

                        eventRef.setValue(record)
                            .addOnSuccessListener {
                                onSuccess(event)
                            }
                            .addOnFailureListener { error ->
                                onFailure(error)
                            }
                    }
            }
    }

    fun cancelSosEvent(
        eventId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateActiveEventStatus(
            eventId = eventId,
            newStatus = "cancelled",
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
            newStatus = "resolved",
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
            onFailure(Exception("You must be logged in to update an SOS event."))
            return
        }

        if (eventId.isBlank()) {
            onFailure(Exception("SOS event ID cannot be empty."))
            return
        }

        val eventRef = sosEventsRef.child(uid).child(eventId)

        eventRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.value == null) {
                    return Transaction.abort()
                }

                val currentStatus = currentData
                    .child("status")
                    .getValue(String::class.java)

                if (currentStatus != "active") {
                    return Transaction.abort()
                }

                currentData.child("status").value = newStatus
                currentData.child("updatedAt").value = System.currentTimeMillis()

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                when {
                    error != null -> onFailure(error.toException())
                    committed -> onSuccess()
                    else -> onFailure(
                        Exception("SOS event does not exist or is no longer active.")
                    )

                }
            }
        })
    }
}