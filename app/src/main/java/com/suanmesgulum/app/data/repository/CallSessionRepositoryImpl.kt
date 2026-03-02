package com.suanmesgulum.app.data.repository

import com.suanmesgulum.app.data.local.dao.CallMessageDao
import com.suanmesgulum.app.data.local.dao.CallSessionDao
import com.suanmesgulum.app.data.mapper.toDomain
import com.suanmesgulum.app.data.mapper.toEntity
import com.suanmesgulum.app.domain.model.CallMessage
import com.suanmesgulum.app.domain.model.CallSession
import com.suanmesgulum.app.domain.repository.CallSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CallSession repository implementasyonu.
 */
@Singleton
class CallSessionRepositoryImpl @Inject constructor(
    private val callSessionDao: CallSessionDao,
    private val callMessageDao: CallMessageDao
) : CallSessionRepository {

    override fun getAllSessions(): Flow<List<CallSession>> {
        return callSessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentSessions(limit: Int): Flow<List<CallSession>> {
        return callSessionDao.getRecentSessions(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSessionById(id: Long): CallSession? {
        return callSessionDao.getSessionById(id)?.toDomain()
    }

    override suspend fun getSessionByCallLogId(callLogId: Long): CallSession? {
        return callSessionDao.getSessionByCallLogId(callLogId)?.toDomain()
    }

    override suspend fun insertSession(session: CallSession): Long {
        return callSessionDao.insertSession(session.toEntity())
    }

    override suspend fun updateSession(session: CallSession) {
        callSessionDao.updateSession(session.toEntity())
    }

    override suspend fun updateSessionStatus(id: Long, status: String, endTime: Long) {
        callSessionDao.updateSessionStatus(id, status, endTime)
    }

    override suspend fun updateTranscript(id: Long, transcript: String) {
        callSessionDao.updateTranscript(id, transcript)
    }

    override suspend fun deleteSession(id: Long) {
        callSessionDao.deleteSession(id)
    }

    // Mesaj işlemleri
    override fun getMessagesBySession(sessionId: Long): Flow<List<CallMessage>> {
        return callMessageDao.getMessagesBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMessagesBySessionSync(sessionId: Long): List<CallMessage> {
        return callMessageDao.getMessagesBySessionSync(sessionId).map { it.toDomain() }
    }

    override suspend fun insertMessage(message: CallMessage): Long {
        return callMessageDao.insertMessage(message.toEntity())
    }

    override suspend fun getLastMessage(sessionId: Long): CallMessage? {
        return callMessageDao.getLastMessage(sessionId)?.toDomain()
    }

    override suspend fun deleteMessagesBySession(sessionId: Long) {
        callMessageDao.deleteMessagesBySession(sessionId)
    }
}
