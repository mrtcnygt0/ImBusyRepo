package com.suanmesgulum.app.data.mapper

import com.suanmesgulum.app.data.local.entity.CallSessionEntity
import com.suanmesgulum.app.domain.model.CallSession
import com.suanmesgulum.app.domain.model.SessionStatus

fun CallSessionEntity.toDomain(): CallSession = CallSession(
    id = id,
    callLogId = callLogId,
    startTime = startTime,
    endTime = endTime,
    recordingPath = recordingPath,
    transcript = transcript,
    status = try { SessionStatus.valueOf(status) } catch (e: Exception) { SessionStatus.DEVAM_EDIYOR }
)

fun CallSession.toEntity(): CallSessionEntity = CallSessionEntity(
    id = id,
    callLogId = callLogId,
    startTime = startTime,
    endTime = endTime,
    recordingPath = recordingPath,
    transcript = transcript,
    status = status.name
)
