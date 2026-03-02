package com.suanmesgulum.app.data.local.dao

import androidx.room.*
import com.suanmesgulum.app.data.local.entity.SpotifyTrackEntity
import kotlinx.coroutines.flow.Flow

/**
 * Spotify şarkıları için DAO.
 * İlk versiyonda sadece altyapı.
 */
@Dao
interface SpotifyTrackDao {

    @Query("SELECT * FROM spotify_tracks ORDER BY name ASC")
    fun getAllTracks(): Flow<List<SpotifyTrackEntity>>

    @Query("SELECT * FROM spotify_tracks WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultTrack(): SpotifyTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: SpotifyTrackEntity)

    @Query("DELETE FROM spotify_tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    @Query("UPDATE spotify_tracks SET isDefault = 0")
    suspend fun clearDefaults()

    @Query("UPDATE spotify_tracks SET isDefault = 1 WHERE id = :id")
    suspend fun setDefault(id: String)
}
