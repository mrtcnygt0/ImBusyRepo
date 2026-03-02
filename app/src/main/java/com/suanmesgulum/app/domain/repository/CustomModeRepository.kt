package com.suanmesgulum.app.domain.repository

import com.suanmesgulum.app.domain.model.CustomMode
import kotlinx.coroutines.flow.Flow

/**
 * Kullanıcı modlarını yönetmek için repository arayüzü.
 */
interface CustomModeRepository {
    fun getAllModes(): Flow<List<CustomMode>>
    suspend fun getModeById(id: Long): CustomMode?
    suspend fun getDefaultMode(): CustomMode?
    suspend fun insertMode(mode: CustomMode): Long
    suspend fun updateMode(mode: CustomMode)
    suspend fun deleteMode(id: Long)
    suspend fun getModeCount(): Int
}
