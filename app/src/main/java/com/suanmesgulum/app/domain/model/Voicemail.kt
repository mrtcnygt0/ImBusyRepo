package com.suanmesgulum.app.domain.model

/**
 * Sesli mesaj domain modeli.
 * Arayan kişinin bıraktığı sesli mesajları temsil eder.
 */
data class Voicemail(
    val id: Long = 0,
    val callerNumber: String,
    val callerName: String? = null,
    val receivedTime: Long = System.currentTimeMillis(),
    val duration: Int = 0,
    val audioFilePath: String,
    val transcript: String? = null,
    val isListened: Boolean = false,
    val isArchived: Boolean = false
)
