package com.example.glimpse.firebase

import android.util.Log
import com.example.glimpse.model.UserLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.FirebaseDatabase
import com.example.glimpse.model.ConnectionRequest
import com.example.glimpse.model.SharingPermissions


class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance(
        "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val locationsRef = database.getReference("locations")
    private val profilesRef = database.getReference("profiles")
    private val glimpseIdsRef = database.getReference("glimpseIds")

    private val connectionRequestsRef = database.getReference("connectionRequests")

    fun updateLocation(
        uid: String,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit = {}
    ) {
        val location = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis()
        )

        Log.d("FIREBASE", "UID = $uid")
        Log.d("FIREBASE", "Database URL = ${database.reference}")

        locationsRef.child(uid)
            .setValue(location)
            .addOnSuccessListener {
                Log.d("FIREBASE", "LOCATION WRITE SUCCESS")

                locationsRef.child(uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        Log.d(
                            "FIREBASE",
                            "LOCATION READ BACK = ${snapshot.value}"
                        )
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "LOCATION READ BACK FAILED",
                            error
                        )
                    }
            }
            .addOnFailureListener { error ->
                Log.e(
                    "FIREBASE",
                    "LOCATION WRITE FAILED",
                    error
                )
            }
    }

    fun getUsersLocations(
        onResult: (List<UserLocation>) -> Unit
    ) {
        locationsRef.get()
            .addOnSuccessListener { snapshot ->

                val locations = snapshot.children.mapNotNull { child ->

                    val location =
                        child.getValue(UserLocation::class.java)

                    location?.copy(
                        uid = child.key ?: ""
                    )
                }

                onResult(locations)
            }
            .addOnFailureListener { error ->
                Log.e(
                    "FIREBASE",
                    "Failed to fetch locations",
                    error
                )
            }
    }

    fun updateProfilePhoto(
        uid: String,
        photoUrl: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        profilesRef
            .child(uid)
            .child("profilePhotoUrl")
            .setValue(photoUrl)
            .addOnSuccessListener {
                Log.d("FIREBASE", "PROFILE PHOTO URL SAVED")
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.e(
                    "FIREBASE",
                    "FAILED TO SAVE PROFILE PHOTO URL",
                    error
                )
                onFailure(error)
            }
    }

    fun getCurrentUserProfile(
        onResult: (name: String, profilePhotoUrl: String) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        database.getReference("profiles")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val name = snapshot.child("name")
                    .getValue(String::class.java) ?: ""

                val profilePhotoUrl = snapshot.child("profilePhotoUrl")
                    .getValue(String::class.java) ?: ""

                onResult(name, profilePhotoUrl)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    fun updateProfileName(
        uid: String,
        name: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        profilesRef
            .child(uid)
            .child("name")
            .setValue(name)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    private fun generateGlimpseId(): String {
        val characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        return buildString {
            repeat(8) {
                append(characters.random())
            }
        }
    }

    fun ensureGlimpseId(
        uid: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        profilesRef
            .child(uid)
            .child("glimpseId")
            .get()
            .addOnSuccessListener { snapshot ->

                val existingId =
                    snapshot.getValue(String::class.java)

                if (!existingId.isNullOrEmpty()) {
                    onSuccess(existingId)
                    return@addOnSuccessListener
                }

                reserveGlimpseId(
                    uid = uid,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    private fun reserveGlimpseId(
        uid: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val glimpseId = generateGlimpseId()

        glimpseIdsRef
            .child(glimpseId)
            .runTransaction(object : Transaction.Handler {

                override fun doTransaction(
                    currentData: MutableData
                ): Transaction.Result {

                    if (currentData.value != null) {
                        return Transaction.abort()
                    }

                    currentData.value = uid

                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {

                    if (error != null) {
                        onFailure(error.toException())
                        return
                    }

                    if (!committed) {
                        reserveGlimpseId(
                            uid = uid,
                            onSuccess = onSuccess,
                            onFailure = onFailure
                        )
                        return
                    }

                    profilesRef
                        .child(uid)
                        .child("glimpseId")
                        .setValue(glimpseId)
                        .addOnSuccessListener {
                            onSuccess(glimpseId)
                        }
                        .addOnFailureListener { saveError ->
                            onFailure(saveError)
                        }
                }
            })
    }

    fun getCurrentUserGlimpseId(
        onResult: (String) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        profilesRef
            .child(uid)
            .child("glimpseId")
            .get()
            .addOnSuccessListener { snapshot ->
                val glimpseId =
                    snapshot.getValue(String::class.java) ?: ""

                onResult(glimpseId)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun findUidByGlimpseId(
        glimpseId: String,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        glimpseIdsRef
            .child(glimpseId.uppercase())
            .get()
            .addOnSuccessListener { snapshot ->
                val uid = snapshot.getValue(String::class.java)
                onSuccess(uid)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    fun sendConnectionRequest(
        senderUid: String,
        receiverUid: String,
        sharingPermissions: SharingPermissions,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        if (senderUid == receiverUid) {
            onFailure(Exception("You cannot connect with yourself"))
            return
        }

        val requestRef = connectionRequestsRef
            .child(receiverUid)
            .child(senderUid)

        requestRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val status = snapshot
                        .child("status")
                        .getValue(String::class.java)

                    if (status == "pending") {
                        onFailure(
                            Exception("Connection request already sent")
                        )
                        return@addOnSuccessListener
                    }
                }

                val request = hashMapOf(
                    "status" to "pending",
                    "createdAt" to System.currentTimeMillis(),
                    "senderSharing" to mapOf(
                        "location" to sharingPermissions.location,
                        "profile" to sharingPermissions.profile,
                        "locationHistory" to sharingPermissions.locationHistory
                    )
                )

                requestRef
                    .setValue(request)
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

    fun getUserProfile(
        uid: String,
        onResult: (name: String, profilePhotoUrl: String) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        profilesRef
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val name = snapshot
                    .child("name")
                    .getValue(String::class.java) ?: ""

                val profilePhotoUrl = snapshot
                    .child("profilePhotoUrl")
                    .getValue(String::class.java) ?: ""

                onResult(name, profilePhotoUrl)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getIncomingConnectionRequests(
        receiverUid: String,
        onResult: (List<ConnectionRequest>) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        connectionRequestsRef
            .child(receiverUid)
            .get()
            .addOnSuccessListener { snapshot ->

                val requests = snapshot.children.mapNotNull { requestSnapshot ->

                    val senderUid = requestSnapshot.key ?: return@mapNotNull null

                    val status = requestSnapshot
                        .child("status")
                        .getValue(String::class.java) ?: ""

                    if (status != "pending") {
                        return@mapNotNull null
                    }

                    val createdAt = requestSnapshot
                        .child("createdAt")
                        .getValue(Long::class.java) ?: 0L

                    val senderSharing = SharingPermissions(
                        location = requestSnapshot
                            .child("senderSharing/location")
                            .getValue(Boolean::class.java) ?: false,
                        profile = requestSnapshot
                            .child("senderSharing/profile")
                            .getValue(Boolean::class.java) ?: false,
                        locationHistory = requestSnapshot
                            .child("senderSharing/locationHistory")
                            .getValue(Boolean::class.java) ?: false
                    )

                    ConnectionRequest(
                        senderUid = senderUid,
                        status = status,
                        createdAt = createdAt,
                        senderSharing = senderSharing
                    )
                }

                if (requests.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val enrichedRequests = mutableListOf<ConnectionRequest>()
                var completed = 0

                requests.forEach { request ->

                    getUserProfile(
                        uid = request.senderUid,
                        onResult = { name, profilePhotoUrl ->

                            enrichedRequests.add(
                                request.copy(
                                    name = name,
                                    profilePhotoUrl = profilePhotoUrl
                                )
                            )

                            completed++

                            if (completed == requests.size) {
                                onResult(
                                    enrichedRequests.sortedByDescending {
                                        it.createdAt
                                    }
                                )
                            }
                        },
                        onFailure = {
                            enrichedRequests.add(request)

                            completed++

                            if (completed == requests.size) {
                                onResult(
                                    enrichedRequests.sortedByDescending {
                                        it.createdAt
                                    }
                                )
                            }
                        }
                    )
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun declineConnectionRequest(
        receiverUid: String,
        senderUid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        connectionRequestsRef
            .child(receiverUid)
            .child(senderUid)
            .removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun acceptConnectionRequest(
        receiverUid: String,
        senderUid: String,
        sharingPermissions: SharingPermissions,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val requestRef = connectionRequestsRef
            .child(receiverUid)
            .child(senderUid)

        val receiverConnectionRef = database
            .getReference("connections")
            .child(receiverUid)
            .child(senderUid)

        val senderConnectionRef = database
            .getReference("connections")
            .child(senderUid)
            .child(receiverUid)

        val receiverSharing = mapOf<String, Any>(
            "location" to sharingPermissions.location,
            "profile" to sharingPermissions.profile,
            "locationHistory" to sharingPermissions.locationHistory
        )

        requestRef.get()
            .addOnSuccessListener { snapshot ->
                val senderSharing = mapOf(
                    "location" to (snapshot.child("senderSharing/location")
                        .getValue(Boolean::class.java) ?: false),
                    "profile" to (snapshot.child("senderSharing/profile")
                        .getValue(Boolean::class.java) ?: false),
                    "locationHistory" to (snapshot.child("senderSharing/locationHistory")
                        .getValue(Boolean::class.java) ?: false)
                )

                val receiverConnection = mapOf(
                    "status" to "connected",
                    "connectedAt" to System.currentTimeMillis(),
                    "sharing" to receiverSharing,
                    "partnerSharing" to senderSharing
                )

                val senderConnection = mapOf(
                    "status" to "connected",
                    "connectedAt" to System.currentTimeMillis(),
                    "sharing" to senderSharing,
                    "partnerSharing" to receiverSharing
                )

                val updates = hashMapOf<String, Any?>(
                    "connections/$receiverUid/$senderUid" to receiverConnection,
                    "connections/$senderUid/$receiverUid" to senderConnection,
                    "connectionRequests/$receiverUid/$senderUid" to null
                )

                database.reference
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onFailure(it)
                    }
            }
    }
    fun getConnections(
        uid: String,
        onResult: (List<ConnectionRequest>) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        database
            .getReference("connections")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val connections = snapshot.children.mapNotNull { connectionSnapshot ->

                    val partnerUid =
                        connectionSnapshot.key ?: return@mapNotNull null

                    val status =
                        connectionSnapshot
                            .child("status")
                            .getValue(String::class.java) ?: ""

                    if (status != "connected") {
                        return@mapNotNull null
                    }

                    ConnectionRequest(
                        senderUid = partnerUid,
                        status = status,
                        createdAt = connectionSnapshot
                            .child("connectedAt")
                            .getValue(Long::class.java) ?: 0L,
                        senderSharing = SharingPermissions(
                            location = connectionSnapshot
                                .child("partnerSharing/location")
                                .getValue(Boolean::class.java) ?: false,

                            profile = connectionSnapshot
                                .child("partnerSharing/profile")
                                .getValue(Boolean::class.java) ?: false,

                            locationHistory = connectionSnapshot
                                .child("partnerSharing/locationHistory")
                                .getValue(Boolean::class.java) ?: false
                        )
                    )
                }

                if (connections.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val enrichedConnections = mutableListOf<ConnectionRequest>()
                var completed = 0

                connections.forEach { connection ->

                    getUserProfile(
                        uid = connection.senderUid,

                        onResult = { name, profilePhotoUrl ->

                            enrichedConnections.add(
                                connection.copy(
                                    name = name,
                                    profilePhotoUrl = profilePhotoUrl
                                )
                            )

                            completed++

                            if (completed == connections.size) {
                                onResult(
                                    enrichedConnections.sortedByDescending {
                                        it.createdAt
                                    }
                                )
                            }
                        },

                        onFailure = {
                            enrichedConnections.add(connection)

                            completed++

                            if (completed == connections.size) {
                                onResult(
                                    enrichedConnections.sortedByDescending {
                                        it.createdAt
                                    }
                                )
                            }
                        }
                    )
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}