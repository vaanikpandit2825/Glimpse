package com.example.glimpse.model

data class SOSEvent(
    val id: String="",
    val userUid: String="",
    val createdAt:Long=0L,
    val updatedAt:Long=0L,
    val status:String="Active",
    val latitude:Double?=null,
    val longitude:Double?=null,
    val locationTimestamp:Long?=null,
    val recipientUids: Map<String, Boolean> = emptyMap()
)