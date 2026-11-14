package com.example.glimpse.firebase

import androidx.compose.animation.core.rememberTransition
import com.example.glimpse.model.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

import com.google.firebase.database.FirebaseDatabase

class EmergencyContactRepository {
    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance("https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val emergencyContactsRef: DatabaseReference =
        database.child("emergencyContacts")
    private fun currentUserUid(): String? {
        return auth.currentUser?.uid
    }

    fun getSentInvitations(
        onSuccess: (List<EmergencyContact>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = currentUserUid()

        if (uid == null) {
            onFailure(
                IllegalStateException("User is not signed in")
            )
            return
        }

        emergencyContactsRef
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val invitations = snapshot.children.mapNotNull {
                    it.getValue(EmergencyContact::class.java)
                }
                onSuccess(invitations)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }

        fun isConnectedTo(
            contactUid: String,
            onResult: (Boolean) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            val requesterUid = currentUserUid() ?: run {
                onFailure(
                    IllegalStateException("User is not signed in")
                )
                return
            }

            if (contactUid.isBlank() || contactUid == requesterUid) {
                onResult(false)
                return
            }
            database
                .child("connections")
                .child(requesterUid)
                .child(contactUid)
                .child("status")
                .get()
                .addOnSuccessListener { snapshot ->
                    val status = snapshot.getValue(String::class.java)

                    onResult(status == "connected")
                }
                .addOnFailureListener { error ->
                    onFailure(error)
                }
        }
        fun getIncomingRequesterUids(
            onSuccess:(List<String>)->Unit,
            onFailure:(Exception)->Unit
        ){
            val uid=currentUserUid()?:run{
                onFailure(
                    IllegalStateException("User is not signed in")
                )
                return
            }
            database
                .child("emergencyContactInbox")
                .child(uid)
                .get()
                .addOnSuccessListener { snapshot->
                    val requesterUids=snapshot.children.mapNotNull {
                        it.key
                    }
                    onSuccess(requesterUids)
                }
                .addOnFailureListener { error->
                    onFailure(error)
                }
        }
    }
}
