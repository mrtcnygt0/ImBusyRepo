package com.suanmesgulum.app.data.mapper

import com.suanmesgulum.app.data.local.entity.CallMessageEntity
import com.suanmesgulum.app.domain.model.CallMessage
import com.suanmesgulum.app.domain.model.Speaker

fun CallMessageEntity.toDomain(): CallMessage = CallMessage(
    id = id,
    sessionId = sessionId,
    timestamp = timestamp,
    speaker = try { Speaker.valueOf(speaker) } catch (e: Exception) { Speaker.ASISTAN },
    messageText = messageText,
    audioFilePath = audioFilePath
)

fun CallMessage.toEntity(): CallMessageEntity = CallMessageEntity(
    id = id,
    sessionId = sessionId,
    timestamp = timestamp,
    speaker = speaker.name,
    messageText = messageText,
    audioFilePath = audioFilePath
)
