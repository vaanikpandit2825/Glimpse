package com.example.glimpse.model

data class SosEmergencyDetails(
    val senderUid: String = "",
    val eventId: String = "",
    val name: String = "",
    val glimpseId: String = "",
    val profilePhotoUrl: String = "",
    val latestLocation: SosEmergencyLocation? = null,
    val updatedAt: Long = 0L,
    val locationFreshAtPublish: Boolean = false
)

data class SosEmergencyLocation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = 0L
)
