package com.suanmesgulum.app.domain.model

/**
 * Arama log kaydı.
 * Her reddedilen ve modla yanıtlanan arama için bir kayıt tutulur.
 */
data class CallLogItem(
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val selectedModeName: String,
    val selectedModeText: String,
    val timestamp: Long,
    val isPaidFeatureUsed: Boolean = false
)
