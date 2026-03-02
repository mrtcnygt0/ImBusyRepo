package com.suanmesgulum.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Arama log kaydı entity'si.
 * Her işlenen arama için bir kayıt tutulur.
 */
@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val selectedModeName: String,
    val selectedModeText: String,
    val timestamp: Long,
    val isPaidFeatureUsed: Boolean = false
)
