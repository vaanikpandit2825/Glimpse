package com.example.glimpse.model

import android.app.DownloadManager.STATUS_PENDING

data class EmergencyContact(
    val requesterId:String="",
    val contactId:String="",
    val status: String = STATUS_PENDING,
    val createdAt:Long=0L,
    val updatedAt:Long=0L
){
    companion object{
        const val STATUS_PENDING="pending"
        const val STATUS_ACCEPTED="accepted"
        const val STATUS_DECLINED="declined"
        const val STATUS_CANCELLED="cancelled"
    }
    val isAccepted: Boolean
        get()=status==STATUS_ACCEPTED
}