package com.suanmesgulum.app.data.local.dao

import androidx.room.*
import com.suanmesgulum.app.data.local.entity.CallMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Çağrı mesajları için DAO.
 */
@Dao
interface CallMessageDao {

    @Query("SELECT * FROM call_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: Long): Flow<List<CallMessageEntity>>

    @Query("SELECT * FROM call_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesBySessionSync(sessionId: Long): List<CallMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CallMessageEntity): Long

    @Query("DELETE FROM call_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: Long)

    @Query("SELECT * FROM call_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(sessionId: Long): CallMessageEntity?
}
