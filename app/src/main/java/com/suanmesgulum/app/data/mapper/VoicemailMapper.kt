package com.suanmesgulum.app.data.mapper

import com.suanmesgulum.app.data.local.entity.VoicemailEntity
import com.suanmesgulum.app.domain.model.Voicemail

fun VoicemailEntity.toDomain(): Voicemail = Voicemail(
    id = id,
    callerNumber = callerNumber,
    callerName = callerName,
    receivedTime = receivedTime,
    duration = duration,
    audioFilePath = audioFilePath,
    transcript = transcript,
    isListened = isListened,
    isArchived = isArchived
)

fun Voicemail.toEntity(): VoicemailEntity = VoicemailEntity(
    id = id,
    callerNumber = callerNumber,
    callerName = callerName,
    receivedTime = receivedTime,
    duration = duration,
    audioFilePath = audioFilePath,
    transcript = transcript,
    isListened = isListened,
    isArchived = isArchived
)
