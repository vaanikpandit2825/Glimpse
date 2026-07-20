package com.example.glimpse.auth

import com.google.firebase.auth.FirebaseAuth

class AuthRepository{
    private val auth  =  FirebaseAuth.getInstance()

    fun signUp(
        email:String,
        password:String,
        onSuccess:()->Unit,
        onFailure:(Exception)->Unit
    ){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener{
                onSuccess()
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
