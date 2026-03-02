package com.suanmesgulum.app.data.local.dao

import androidx.room.*
import com.suanmesgulum.app.data.local.entity.AssistantSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Asistan ayarları için DAO.
 * Tek satırlık tablo (id = 1).
 */
@Dao
interface AssistantSettingsDao {

    @Query("SELECT * FROM assistant_settings WHERE id = 1")
    fun getSettings(): Flow<AssistantSettingsEntity?>

    @Query("SELECT * FROM assistant_settings WHERE id = 1")
    suspend fun getSettingsSync(): AssistantSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AssistantSettingsEntity)

    @Query("UPDATE assistant_settings SET assistantName = :name WHERE id = 1")
    suspend fun updateAssistantName(name: String)

    @Query("UPDATE assistant_settings SET personality = :personality WHERE id = 1")
    suspend fun updatePersonality(personality: String)

    @Query("UPDATE assistant_settings SET defaultLanguage = :language WHERE id = 1")
    suspend fun updateDefaultLanguage(language: String)

    @Query("UPDATE assistant_settings SET defaultVoiceId = :voiceId WHERE id = 1")
    suspend fun updateDefaultVoice(voiceId: String)

    @Query("UPDATE assistant_settings SET defaultGreetingMessageId = :modeId WHERE id = 1")
    suspend fun updateDefaultGreeting(modeId: Long)

    @Query("UPDATE assistant_settings SET infoGatheringMessage = :message WHERE id = 1")
    suspend fun updateInfoGatheringMessage(message: String)

    @Query("UPDATE assistant_settings SET farewellMessage = :message WHERE id = 1")
    suspend fun updateFarewellMessage(message: String)

    @Query("UPDATE assistant_settings SET voicemailPromptMessage = :message WHERE id = 1")
    suspend fun updateVoicemailPromptMessage(message: String)

    @Query("UPDATE assistant_settings SET spotifyConnected = :connected, spotifyPlaylistId = :playlistId WHERE id = 1")
    suspend fun updateSpotifySettings(connected: Boolean, playlistId: String?)
}
