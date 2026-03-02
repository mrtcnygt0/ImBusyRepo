package com.suanmesgulum.app.domain.repository

import com.suanmesgulum.app.domain.model.Voicemail
import kotlinx.coroutines.flow.Flow

/**
 * Sesli mesajları yönetmek için repository arayüzü.
 */
interface VoicemailRepository {
    fun getAllVoicemails(): Flow<List<Voicemail>>
    fun getArchivedVoicemails(): Flow<List<Voicemail>>
    fun getUnlistenedCount(): Flow<Int>
    suspend fun getVoicemailById(id: Long): Voicemail?
    suspend fun insertVoicemail(voicemail: Voicemail): Long
    suspend fun markAsListened(id: Long)
    suspend fun archiveVoicemail(id: Long)
    suspend fun deleteVoicemail(id: Long)
    suspend fun clearAllVoicemails()
}
