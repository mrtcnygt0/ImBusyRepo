package com.suanmesgulum.app.data.repository

import com.suanmesgulum.app.data.local.dao.CustomModeDao
import com.suanmesgulum.app.data.mapper.toDomain
import com.suanmesgulum.app.data.mapper.toEntity
import com.suanmesgulum.app.domain.model.CustomMode
import com.suanmesgulum.app.domain.repository.CustomModeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CustomMode repository implementasyonu.
 * Room DAO aracılığıyla veritabanı işlemlerini yürütür.
 */
@Singleton
class CustomModeRepositoryImpl @Inject constructor(
    private val customModeDao: CustomModeDao
) : CustomModeRepository {

    override fun getAllModes(): Flow<List<CustomMode>> {
        return customModeDao.getAllModes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getModeById(id: Long): CustomMode? {
        return customModeDao.getModeById(id)?.toDomain()
    }

    override suspend fun getDefaultMode(): CustomMode? {
        return customModeDao.getDefaultMode()?.toDomain()
    }

    override suspend fun insertMode(mode: CustomMode): Long {
        return customModeDao.insertMode(mode.toEntity())
    }

    override suspend fun updateMode(mode: CustomMode) {
        customModeDao.updateMode(mode.toEntity())
    }

    override suspend fun deleteMode(id: Long) {
        customModeDao.deleteMode(id)
    }

    override suspend fun getModeCount(): Int {
        return customModeDao.getModeCount()
    }
}
