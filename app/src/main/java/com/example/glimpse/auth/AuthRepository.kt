package com.example.glimpse.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    private val database = FirebaseDatabase.getInstance(
        "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val profilesRef = database.getReference("profiles")

    fun signUp(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid

                if (uid == null) {
                    onFailure(Exception("User ID is null"))
                    return@addOnSuccessListener
                }

                val profile = mapOf(
                    "name" to name,
                    "profilePhotoUrl" to ""
                )

                profilesRef
                    .child(uid)
                    .setValue(profile)
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

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}