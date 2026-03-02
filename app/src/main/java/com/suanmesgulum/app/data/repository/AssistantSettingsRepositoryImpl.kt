package com.suanmesgulum.app.data.repository

import com.suanmesgulum.app.data.local.dao.AssistantSettingsDao
import com.suanmesgulum.app.data.local.entity.AssistantSettingsEntity
import com.suanmesgulum.app.data.mapper.toDomain
import com.suanmesgulum.app.data.mapper.toEntity
import com.suanmesgulum.app.domain.model.AssistantSettings
import com.suanmesgulum.app.domain.repository.AssistantSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AssistantSettings repository implementasyonu.
 */
@Singleton
class AssistantSettingsRepositoryImpl @Inject constructor(
    private val assistantSettingsDao: AssistantSettingsDao
) : AssistantSettingsRepository {

    override fun getSettings(): Flow<AssistantSettings?> {
        return assistantSettingsDao.getSettings().map { it?.toDomain() }
    }

    override suspend fun getSettingsSync(): AssistantSettings? {
        return assistantSettingsDao.getSettingsSync()?.toDomain()
    }

    override suspend fun saveSettings(settings: AssistantSettings) {
        assistantSettingsDao.insertOrUpdate(settings.toEntity())
    }

    override suspend fun updateAssistantName(name: String) {
        ensureDefaultSettings()
        assistantSettingsDao.updateAssistantName(name)
    }

    override suspend fun updatePersonality(personality: String) {
        ensureDefaultSettings()
        assistantSettingsDao.updatePersonality(personality)
    }

    override suspend fun updateDefaultLanguage(language: String) {
        ensureDefaultSettings()
        assistantSettingsDao.updateDefaultLanguage(language)
    }

    override suspend fun updateDefaultVoice(voiceId: String) {
        ensureDefaultSettings()
        assistantSettingsDao.updateDefaultVoice(voiceId)
    }

    override suspend fun updateDefaultGreeting(modeId: Long) {
        ensureDefaultSettings()
        assistantSettingsDao.updateDefaultGreeting(modeId)
    }

    override suspend fun updateInfoGatheringMessage(message: String) {
        ensureDefaultSettings()
        assistantSettingsDao.updateInfoGatheringMessage(message)
    }

    override suspend fun updateFarewellMessage(message: String) {
        ensureDefaultSettings()
        assistantSettingsDao.updateFarewellMessage(message)
    }

    override suspend fun updateVoicemailPromptMessage(message: String) {
        ensureDefaultSettings()
        assistantSettingsDao.updateVoicemailPromptMessage(message)
    }

    override suspend fun ensureDefaultSettings() {
        if (assistantSettingsDao.getSettingsSync() == null) {
            assistantSettingsDao.insertOrUpdate(AssistantSettingsEntity())
        }
    }
}
