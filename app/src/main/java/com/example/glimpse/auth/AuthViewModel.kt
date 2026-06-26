package com.example.glimpse.auth

import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel(){

    private val repository= AuthRepository()

    fun signUp(
        email:String,
        password:String,
        onSuccess:()->Unit,
        onFailure:(String)->Unit
    ){
        repository.signUp(
            email=email,
            password=password,
            onSuccess=onSuccess,
            onFailure=onFailure
        )
    }
}