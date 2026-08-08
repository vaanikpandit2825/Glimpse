package com.example.glimpse.firebase

import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import com.google.firebase.FirebaseApp

class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance(
        "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    private val usersRef = database.getReference("users")

    fun updateLocation(
        uid: String,
        latitude: Double,
        longitude: Double
    ) {
        val location = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis()
        )

        Log.d("FIREBASE", "UID = $uid")
        Log.d("FIREBASE", "Database URL = ${database.reference}")

        usersRef.child(uid)
            .setValue(location)
            .addOnSuccessListener {
                Log.d("FIREBASE", "WRITE SUCCESS")

                usersRef.child(uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        Log.d("FIREBASE", "READ BACK = ${snapshot.value}")
                    }
                    .addOnFailureListener { error ->
                        Log.e("FIREBASE", "READ BACK FAILED", error)
                    }
            }
            .addOnFailureListener { error ->
                Log.e("FIREBASE", "WRITE FAILED", error)
            }
    }
}