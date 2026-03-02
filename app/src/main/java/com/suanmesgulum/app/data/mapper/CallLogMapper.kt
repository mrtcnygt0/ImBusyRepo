package com.suanmesgulum.app.data.mapper

import com.suanmesgulum.app.data.local.entity.CallLogEntity
import com.suanmesgulum.app.domain.model.CallLogItem

/**
 * Entity <-> Domain model dönüşümleri.
 */
fun CallLogEntity.toDomain(): CallLogItem = CallLogItem(
    id = id,
    phoneNumber = phoneNumber,
    contactName = contactName,
    selectedModeName = selectedModeName,
    selectedModeText = selectedModeText,
    timestamp = timestamp,
    isPaidFeatureUsed = isPaidFeatureUsed
)

fun CallLogItem.toEntity(): CallLogEntity = CallLogEntity(
    id = id,
    phoneNumber = phoneNumber,
    contactName = contactName,
    selectedModeName = selectedModeName,
    selectedModeText = selectedModeText,
    timestamp = timestamp,
    isPaidFeatureUsed = isPaidFeatureUsed
)
