
package com.example.glimpse.model

data class EmergencyContact(
    val requesterId: String = "",
    val contactId: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val voiceCallConsent: Boolean = false
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_DECLINED = "declined"
        const val STATUS_CANCELLED = "cancelled"
    }

    val isAccepted: Boolean
        get() = status == STATUS_ACCEPTED
}
