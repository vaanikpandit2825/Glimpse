package com.example.glimpse.firebase

import android.util.Log
import com.example.glimpse.model.UserLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.FirebaseDatabase

class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance(
        "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val locationsRef = database.getReference("locations")
    private val profilesRef = database.getReference("profiles")
    private val glimpseIdsRef = database.getReference("glimpseIds")

    fun updateLocation(
        uid: String,
        latitude: Double,
        longitude: Double,
        onSuccess:()->Unit={}
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

}