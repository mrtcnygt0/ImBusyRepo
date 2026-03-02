package com.suanmesgulum.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Spotify şarkı entity'si.
 * Bekletme müziği olarak seçilen şarkıları saklar.
 * İlk versiyonda sadece altyapı.
 */
@Entity(tableName = "spotify_tracks")
data class SpotifyTrackEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val artist: String,
    val albumArtUrl: String? = null,
    val isDefault: Boolean = false
)
