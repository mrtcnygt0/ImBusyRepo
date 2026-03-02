package com.suanmesgulum.app.data.local.dao

import androidx.room.*
import com.suanmesgulum.app.data.local.entity.CallLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Arama logları için DAO (Data Access Object).
 */
@Dao
interface CallLogDao {

    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getLogsSince(startOfDay: Long): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CallLogEntity): Long

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM call_logs WHERE timestamp >= :startOfDay")
    suspend fun getCallCountSince(startOfDay: Long): Int

    @Query("""
        SELECT selectedModeName FROM call_logs 
        GROUP BY selectedModeName 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getMostUsedModeName(): String?
}
