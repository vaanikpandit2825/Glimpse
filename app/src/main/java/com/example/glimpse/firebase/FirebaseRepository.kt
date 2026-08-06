package com.example.glimpse.firebase

import com.google.firebase.database.FirebaseDatabase

class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance()
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
        usersRef.child(uid).setValue(location)
    }
}