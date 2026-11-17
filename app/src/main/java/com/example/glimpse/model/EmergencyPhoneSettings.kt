package com.example.glimpse.model

data class EmergencyPhoneSettings (
    val phoneNumber:String="",
    val verified: Boolean=false,
    val verifiedAt:Long=0L,
    val updatedAt:Long=0L
    )