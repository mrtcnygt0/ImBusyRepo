package com.suanmesgulum.app.service.spotify

import android.content.Context
import android.util.Log
import com.suanmesgulum.app.service.audio.MusicPlayer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Spotify Manager — İlk versiyonda boş stub.
 *
 * İleride Spotify OAuth, şarkı arama, çalma ve bekletme müziği
 * özellikleri bu sınıfa eklenecek.
 *
 * Spotify SDK entegrasyonu karmaşık olduğu için ilk versiyona
 * dahil edilmemiştir. Sadece arayüz altyapısı hazırlanmıştır.
 */
@Singleton
class SpotifyManager @Inject constructor(
    private val context: Context
) : MusicPlayer {

    companion object {
        private const val TAG = "SpotifyManager"
    }

    override val playerName: String = "Spotify"

    private var _isConnected = false
    override val isConnected: Boolean get() = _isConnected

    /**
     * Spotify OAuth ile bağlan.
     * İlk versiyonda her zaman false döndürür.
     */
    override suspend fun connect(): Boolean {
        Log.i(TAG, "Spotify bağlantısı — İlk versiyonda desteklenmiyor")
        // TODO: Spotify SDK entegrasyonu
        // val authRequest = AuthorizationRequest.Builder(...)
        return false
    }

    /**
     * Şarkı çal.
     * İlk versiyonda her zaman false döndürür.
     */
    override suspend fun play(trackId: String): Boolean {
        Log.i(TAG, "Spotify çalma — İlk versiyonda desteklenmiyor: $trackId")
        // TODO: SpotifyAppRemote.playerApi.play(trackId)
        return false
    }

    override fun pause() {
        Log.i(TAG, "Spotify duraklat — İlk versiyonda desteklenmiyor")
        // TODO: SpotifyAppRemote.playerApi.pause()
    }

    override fun stop() {
        Log.i(TAG, "Spotify durdur — İlk versiyonda desteklenmiyor")
        // TODO: SpotifyAppRemote.playerApi.pause()
    }

    override fun disconnect() {
        _isConnected = false
        Log.i(TAG, "Spotify bağlantısı kesildi")
        // TODO: SpotifyAppRemote.disconnect()
    }

    /**
     * Şarkı ara.
     * İlk versiyonda boş liste döndürür.
     */
    suspend fun searchTracks(query: String): List<Any> {
        Log.i(TAG, "Spotify arama — İlk versiyonda desteklenmiyor: $query")
        // TODO: Spotify Web API ile arama
        return emptyList()
    }

    /**
     * Seçilen şarkıyı veritabanına kaydet.
     */
    suspend fun saveTrack(trackId: String, name: String, artist: String) {
        Log.i(TAG, "Şarkı kaydet — İlk versiyonda desteklenmiyor: $name by $artist")
        // TODO: SpotifyTrackDao ile kaydet
    }
}
