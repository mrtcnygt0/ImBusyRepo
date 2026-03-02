package com.suanmesgulum.app.domain.repository

import com.suanmesgulum.app.domain.model.AssistantSettings
import kotlinx.coroutines.flow.Flow

/**
 * Asistan ayarlarını yönetmek için repository arayüzü.
 */
interface AssistantSettingsRepository {
    fun getSettings(): Flow<AssistantSettings?>
    suspend fun getSettingsSync(): AssistantSettings?
    suspend fun saveSettings(settings: AssistantSettings)
    suspend fun updateAssistantName(name: String)
    suspend fun updatePersonality(personality: String)
    suspend fun updateDefaultLanguage(language: String)
    suspend fun updateDefaultVoice(voiceId: String)
    suspend fun updateDefaultGreeting(modeId: Long)
    suspend fun updateInfoGatheringMessage(message: String)
    suspend fun updateFarewellMessage(message: String)
    suspend fun updateVoicemailPromptMessage(message: String)
    suspend fun ensureDefaultSettings()
}
