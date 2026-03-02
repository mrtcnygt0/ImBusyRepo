package com.suanmesgulum.app.domain.repository

import com.suanmesgulum.app.domain.model.CallLogItem
import kotlinx.coroutines.flow.Flow

/**
 * Arama loglarını yönetmek için repository arayüzü.
 */
interface CallLogRepository {
    fun getAllLogs(): Flow<List<CallLogItem>>
    fun getTodayLogs(): Flow<List<CallLogItem>>
    suspend fun insertLog(log: CallLogItem): Long
    suspend fun deleteLog(id: Long)
    suspend fun clearAllLogs()
    suspend fun getTodayCallCount(): Int
    suspend fun getMostUsedModeName(): String?
}
