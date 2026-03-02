package com.suanmesgulum.app.data.mapper

import com.suanmesgulum.app.data.local.entity.AssistantSettingsEntity
import com.suanmesgulum.app.domain.model.AssistantSettings

fun AssistantSettingsEntity.toDomain(): AssistantSettings = AssistantSettings(
    id = id,
    assistantName = assistantName,
    personality = personality,
    defaultGreetingMessageId = defaultGreetingMessageId,
    defaultVoiceId = defaultVoiceId,
    defaultLanguage = defaultLanguage,
    spotifyConnected = spotifyConnected,
    spotifyPlaylistId = spotifyPlaylistId,
    infoGatheringMessage = infoGatheringMessage,
    farewellMessage = farewellMessage,
    voicemailPromptMessage = voicemailPromptMessage
)

fun AssistantSettings.toEntity(): AssistantSettingsEntity = AssistantSettingsEntity(
    id = id,
    assistantName = assistantName,
    personality = personality,
    defaultGreetingMessageId = defaultGreetingMessageId,
    defaultVoiceId = defaultVoiceId,
    defaultLanguage = defaultLanguage,
    spotifyConnected = spotifyConnected,
    spotifyPlaylistId = spotifyPlaylistId,
    infoGatheringMessage = infoGatheringMessage,
    farewellMessage = farewellMessage,
    voicemailPromptMessage = voicemailPromptMessage
)
