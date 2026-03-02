package com.suanmesgulum.app.data.repository

import com.suanmesgulum.app.data.local.dao.VoicemailDao
import com.suanmesgulum.app.data.mapper.toDomain
import com.suanmesgulum.app.data.mapper.toEntity
import com.suanmesgulum.app.domain.model.Voicemail
import com.suanmesgulum.app.domain.repository.VoicemailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voicemail repository implementasyonu.
 */
@Singleton
class VoicemailRepositoryImpl @Inject constructor(
    private val voicemailDao: VoicemailDao
) : VoicemailRepository {

    override fun getAllVoicemails(): Flow<List<Voicemail>> {
        return voicemailDao.getAllVoicemails().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedVoicemails(): Flow<List<Voicemail>> {
        return voicemailDao.getArchivedVoicemails().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnlistenedCount(): Flow<Int> {
        return voicemailDao.getUnlistenedCount()
    }

    override suspend fun getVoicemailById(id: Long): Voicemail? {
        return voicemailDao.getVoicemailById(id)?.toDomain()
    }

    override suspend fun insertVoicemail(voicemail: Voicemail): Long {
        return voicemailDao.insertVoicemail(voicemail.toEntity())
    }

    override suspend fun markAsListened(id: Long) {
        voicemailDao.markAsListened(id)
    }

    override suspend fun archiveVoicemail(id: Long) {
        voicemailDao.archiveVoicemail(id)
    }

    override suspend fun deleteVoicemail(id: Long) {
        voicemailDao.deleteVoicemail(id)
    }

    override suspend fun clearAllVoicemails() {
        voicemailDao.clearAllVoicemails()
    }
}
