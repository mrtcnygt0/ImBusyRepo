package com.suanmesgulum.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Çağrı oturumu entity'si.
 * Asistanın cevapladığı her arama için bir oturum kaydı tutulur.
 */
@Entity(
    tableName = "call_sessions",
    foreignKeys = [
        ForeignKey(
            entity = CallLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["callLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("callLogId")]
)
data class CallSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val callLogId: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0,
    val recordingPath: String? = null,
    val transcript: String? = null,
    val status: String = "DEVAM_EDIYOR"
)
