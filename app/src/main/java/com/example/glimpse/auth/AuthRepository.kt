package com.example.glimpse.auth

import com.example.glimpse.firebase.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.glimpse.firebase.FCMTokenRepository
import com.example.glimpse.firebase.GlimpseDatabase

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    private val database = 
GlimpseDatabase.instance

    private val firebaseRepository=FirebaseRepository()

    private val profilesRef=database.getReference("profiles")
    private val fcmTokenRepository= FCMTokenRepository()

    fun signUp(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        )
            .addOnSuccessListener { result ->

                val uid = result.user?.uid

                if (uid == null) {
                    onFailure(Exception("Firebase created the account but returned no user ID"))
                    return@addOnSuccessListener
                }

                val profile = mapOf(
                    "name" to name.trim(),
                    "profilePhotoUrl" to ""
                )

                profilesRef
                    .child(uid)
                    .setValue(profile)
                    .addOnSuccessListener {

                        firebaseRepository.ensureGlimpseId(
                            uid = uid,
                            onSuccess = {
                                fcmTokenRepository.registerCurrentDevice(
                                    onSuccess = {
                                        android.util.Log.d(
                                            "GLIMPSE_FCM",
                                            "Token registration completed after signup"
                                        )
                                        onSuccess()
                                    },
                                    onFailure = { exception ->
                                        android.util.Log.e(
                                            "GLIMPSE_FCM",
                                            "Token registration failed after signup",
                                            exception
                                        )
                                        onSuccess()
                                    }
                                )
                            },
                            onFailure = {
                                onFailure(it)
                            }
                        )
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        )
            .addOnSuccessListener {

                fcmTokenRepository.registerCurrentDevice(
                    onSuccess = {
                        onSuccess()
                    },
                    onFailure = { exception ->
                        android.util.Log.e(
                            "GLIMPSE_FCM",
                            "Token registration failed during login",
                            exception
                        )
                        onSuccess()
                    }
                )
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}