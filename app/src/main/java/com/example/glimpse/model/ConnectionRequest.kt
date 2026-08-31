package com.example.glimpse.model

data class ConnectionRequest(
    val senderUid: String = "",
    val status: String = "",
    val createdAt: Long = 0L,
    val name: String = "",
    val profilePhotoUrl: String = "",
    val senderSharing: SharingPermissions = SharingPermissions()
)