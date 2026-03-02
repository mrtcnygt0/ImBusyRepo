package com.suanmesgulum.app.data.local.dao

import androidx.room.*
import com.suanmesgulum.app.data.local.entity.CallSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Çağrı oturumları için DAO.
 */
@Dao
interface CallSessionDao {

    @Query("SELECT * FROM call_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<CallSessionEntity>>

    @Query("SELECT * FROM call_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): CallSessionEntity?

    @Query("SELECT * FROM call_sessions WHERE callLogId = :callLogId")
    suspend fun getSessionByCallLogId(callLogId: Long): CallSessionEntity?

    @Query("SELECT * FROM call_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<CallSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CallSessionEntity): Long

    @Update
    suspend fun updateSession(session: CallSessionEntity)

    @Query("DELETE FROM call_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("UPDATE call_sessions SET status = :status, endTime = :endTime WHERE id = :id")
    suspend fun updateSessionStatus(id: Long, status: String, endTime: Long)

    @Query("UPDATE call_sessions SET transcript = :transcript WHERE id = :id")
    suspend fun updateTranscript(id: Long, transcript: String)
}
