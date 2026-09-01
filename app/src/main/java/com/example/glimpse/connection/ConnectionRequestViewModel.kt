package com.example.glimpse.connection

import androidx.lifecycle.ViewModel
import com.example.glimpse.firebase.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.example.glimpse.model.ConnectionRequest
import com.example.glimpse.model.SharingPermissions

class ConnectionRequestViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    fun sendConnectionRequest(
        receiverUid: String,
        onSuccess: () -> Unit,
        permissions: SharingPermissions,
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
            sharingPermissions = permissions,
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
    fun getIncomingConnectionRequests(
        onResult: (List<com.example.glimpse.model.ConnectionRequest>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val receiverUid =
            FirebaseAuth.getInstance().currentUser?.uid

        if (receiverUid == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        repository.getIncomingConnectionRequests(
            receiverUid = receiverUid,
            onResult = onResult,
            onFailure = onFailure
        )
    }

    fun declineConnectionRequest(
        senderUid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val receiverUid =
            FirebaseAuth.getInstance().currentUser?.uid

        if (receiverUid == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        repository.declineConnectionRequest(
            receiverUid = receiverUid,
            senderUid = senderUid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun acceptConnectionRequest(
        senderUid: String,
        permissions: com.example.glimpse.model.SharingPermissions,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val receiverUid =
            FirebaseAuth.getInstance().currentUser?.uid

        if (receiverUid == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        repository.acceptConnectionRequest(
            receiverUid = receiverUid,
            senderUid = senderUid,
            sharingPermissions = permissions,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}