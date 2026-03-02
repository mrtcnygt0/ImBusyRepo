package com.suanmesgulum.app.data.local.dao

import androidx.room.*
import com.suanmesgulum.app.data.local.entity.CustomModeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Kullanıcı modları için DAO (Data Access Object).
 */
@Dao
interface CustomModeDao {

    @Query("SELECT * FROM custom_modes ORDER BY orderIndex ASC")
    fun getAllModes(): Flow<List<CustomModeEntity>>

    @Query("SELECT * FROM custom_modes WHERE id = :id")
    suspend fun getModeById(id: Long): CustomModeEntity?

    @Query("SELECT * FROM custom_modes WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultMode(): CustomModeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: CustomModeEntity): Long

    @Update
    suspend fun updateMode(mode: CustomModeEntity)

    @Query("DELETE FROM custom_modes WHERE id = :id")
    suspend fun deleteMode(id: Long)

    @Query("SELECT COUNT(*) FROM custom_modes")
    suspend fun getModeCount(): Int

    @Query("SELECT * FROM custom_modes ORDER BY orderIndex ASC LIMIT 3")
    suspend fun getTopModes(): List<CustomModeEntity>
}
