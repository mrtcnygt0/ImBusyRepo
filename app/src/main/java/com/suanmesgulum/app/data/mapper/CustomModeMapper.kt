package com.suanmesgulum.app.data.mapper

import com.suanmesgulum.app.data.local.entity.CustomModeEntity
import com.suanmesgulum.app.domain.model.CustomMode

/**
 * Entity <-> Domain model dönüşümleri.
 */
fun CustomModeEntity.toDomain(): CustomMode = CustomMode(
    id = id,
    name = name,
    text = text,
    orderIndex = orderIndex,
    isDefault = isDefault
)

fun CustomMode.toEntity(): CustomModeEntity = CustomModeEntity(
    id = id,
    name = name,
    text = text,
    orderIndex = orderIndex,
    isDefault = isDefault
)
