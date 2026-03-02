package com.suanmesgulum.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Çağrı mesajı entity'si.
 * Bir oturumdaki her konuşma parçasını saklar.
 */
@Entity(
    tableName = "call_messages",
    foreignKeys = [
        ForeignKey(
            entity = CallSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class CallMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val speaker: String, // "ASISTAN" veya "ARAYAN"
    val messageText: String? = null,
    val audioFilePath: String? = null
)
