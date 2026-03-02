package com.suanmesgulum.app.domain.repository

import com.suanmesgulum.app.domain.model.CallMessage
import com.suanmesgulum.app.domain.model.CallSession
import kotlinx.coroutines.flow.Flow

/**
 * Çağrı oturumu ve mesajları yönetmek için repository arayüzü.
 */
interface CallSessionRepository {
    fun getAllSessions(): Flow<List<CallSession>>
    fun getRecentSessions(limit: Int = 10): Flow<List<CallSession>>
    suspend fun getSessionById(id: Long): CallSession?
    suspend fun getSessionByCallLogId(callLogId: Long): CallSession?
    suspend fun insertSession(session: CallSession): Long
    suspend fun updateSession(session: CallSession)
    suspend fun updateSessionStatus(id: Long, status: String, endTime: Long)
    suspend fun updateTranscript(id: Long, transcript: String)
    suspend fun deleteSession(id: Long)

    // Mesaj işlemleri
    fun getMessagesBySession(sessionId: Long): Flow<List<CallMessage>>
    suspend fun getMessagesBySessionSync(sessionId: Long): List<CallMessage>
    suspend fun insertMessage(message: CallMessage): Long
    suspend fun getLastMessage(sessionId: Long): CallMessage?
    suspend fun deleteMessagesBySession(sessionId: Long)
}
