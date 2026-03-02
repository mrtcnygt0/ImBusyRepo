package com.suanmesgulum.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Kullanıcının tanımladığı meşgul modu entity'si.
 * Room veritabanında saklanır.
 */
@Entity(tableName = "custom_modes")
data class CustomModeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val text: String,
    val orderIndex: Int = 0,
    val isDefault: Boolean = false
)
