package com.example.glimpse.connection

import androidx.lifecycle.ViewModel
import com.example.glimpse.firebase.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth

class ConnectionRequestViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    fun sendConnectionRequest(
        receiverUid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val senderUid =
            FirebaseAuth.getInstance().currentUser?.uid

        if (senderUid == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        repository.sendConnectionRequest(
            senderUid = senderUid,
            receiverUid = receiverUid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
    fun getUserProfile(
        uid:String,
        onResult: (name:String, profilePhoto:String) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        repository.getUserProfile(
            uid=uid,
            onResult=onResult,
            onFailure=onFailure
        )

    }
}