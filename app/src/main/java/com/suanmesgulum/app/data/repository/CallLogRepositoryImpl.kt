package com.suanmesgulum.app.data.repository

import com.suanmesgulum.app.data.local.dao.CallLogDao
import com.suanmesgulum.app.data.mapper.toDomain
import com.suanmesgulum.app.data.mapper.toEntity
import com.suanmesgulum.app.domain.model.CallLogItem
import com.suanmesgulum.app.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CallLog repository implementasyonu.
 * Arama loglarını Room DB aracılığıyla yönetir.
 */
@Singleton
class CallLogRepositoryImpl @Inject constructor(
    private val callLogDao: CallLogDao
) : CallLogRepository {

    override fun getAllLogs(): Flow<List<CallLogItem>> {
        return callLogDao.getAllLogs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTodayLogs(): Flow<List<CallLogItem>> {
        val startOfDay = getStartOfDay()
        return callLogDao.getLogsSince(startOfDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertLog(log: CallLogItem): Long {
        return callLogDao.insertLog(log.toEntity())
    }

    override suspend fun deleteLog(id: Long) {
        callLogDao.deleteLog(id)
    }

    override suspend fun clearAllLogs() {
        callLogDao.clearAllLogs()
    }

    override suspend fun getTodayCallCount(): Int {
        return callLogDao.getCallCountSince(getStartOfDay())
    }

    override suspend fun getMostUsedModeName(): String? {
        return callLogDao.getMostUsedModeName()
    }

    /**
     * Bugünün başlangıç zamanını (00:00:00) milisaniye olarak döndürür.
     */
    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
