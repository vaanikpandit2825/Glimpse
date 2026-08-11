package com.example.glimpse.auth

import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel(){

    private val repository= AuthRepository()

    fun signUp(
        name: String,
        email:String,
        password:String,
        onSuccess:()->Unit,
        onFailure:(Exception)->Unit
    ){
        repository.signUp(
            name,
            email,
            password,
            onSuccess,
            onFailure
        )
    }
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.login(
            email,
            password,
            onSuccess,
            onFailure
        )
    }
}