package com.suanmesgulum.app.data.local.dao

import androidx.room.*
import com.suanmesgulum.app.data.local.entity.VoicemailEntity
import kotlinx.coroutines.flow.Flow

/**
 * Sesli mesajlar için DAO.
 */
@Dao
interface VoicemailDao {

    @Query("SELECT * FROM voicemails WHERE isArchived = 0 ORDER BY receivedTime DESC")
    fun getAllVoicemails(): Flow<List<VoicemailEntity>>

    @Query("SELECT * FROM voicemails WHERE isArchived = 1 ORDER BY receivedTime DESC")
    fun getArchivedVoicemails(): Flow<List<VoicemailEntity>>

    @Query("SELECT * FROM voicemails WHERE id = :id")
    suspend fun getVoicemailById(id: Long): VoicemailEntity?

    @Query("SELECT COUNT(*) FROM voicemails WHERE isListened = 0 AND isArchived = 0")
    fun getUnlistenedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoicemail(voicemail: VoicemailEntity): Long

    @Update
    suspend fun updateVoicemail(voicemail: VoicemailEntity)

    @Query("UPDATE voicemails SET isListened = 1 WHERE id = :id")
    suspend fun markAsListened(id: Long)

    @Query("UPDATE voicemails SET isArchived = 1 WHERE id = :id")
    suspend fun archiveVoicemail(id: Long)

    @Query("DELETE FROM voicemails WHERE id = :id")
    suspend fun deleteVoicemail(id: Long)

    @Query("DELETE FROM voicemails")
    suspend fun clearAllVoicemails()
}
