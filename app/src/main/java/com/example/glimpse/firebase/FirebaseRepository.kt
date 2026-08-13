package com.example.glimpse.firebase

import android.util.Log
import com.example.glimpse.model.UserLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance(
        "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val locationsRef = database.getReference("locations")

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
}