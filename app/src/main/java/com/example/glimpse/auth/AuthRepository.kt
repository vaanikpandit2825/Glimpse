package com.example.glimpse.auth

import com.google.firebase.auth.FirebaseAuth

class AuthRepository{
    private val auth  =  FirebaseAuth.getInstance()

    fun signUp(
        email:String,
        password:String,
        onSuccess:()->Unit,
        onFailure:(String)->Unit
    ){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener{
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Unknown Error")
            }
    }
}
