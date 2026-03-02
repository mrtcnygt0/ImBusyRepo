package com.suanmesgulum.app.domain.model

/**
 * Spotify şarkı domain modeli.
 * Bekletme müziği olarak seçilen şarkıyı temsil eder.
 * İlk versiyonda sadece altyapı — Spotify entegrasyonu ileride yapılacak.
 */
data class SpotifyTrack(
    val id: String,
    val name: String,
    val artist: String,
    val albumArtUrl: String? = null,
    val isDefault: Boolean = false
)
