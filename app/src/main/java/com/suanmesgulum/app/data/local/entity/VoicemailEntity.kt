package com.suanmesgulum.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sesli mesaj entity'si.
 * Arayanların bıraktığı sesli mesajları saklar.
 */
@Entity(tableName = "voicemails")
data class VoicemailEntity(
    @PrimaryKey(autoGenerate = true)
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
