package com.suanmesgulum.app.service.audio

/**
 * Müzik oynatıcı arayüzü.
 * İleride Spotify ve diğer müzik kaynakları bu arayüzü implemente edecek.
 */
interface MusicPlayer {
    /** Oynatıcı adı */
    val playerName: String

    /** Oynatıcı bağlı ve kullanılabilir mi */
    val isConnected: Boolean

    /** Müzik çalmaya başla */
    suspend fun play(trackId: String): Boolean

    /** Müzik çalmayı duraklat */
    fun pause()

    /** Müzik çalmayı durdur */
    fun stop()

    /** Oynatıcıya bağlan */
    suspend fun connect(): Boolean

    /** Bağlantıyı kes */
    fun disconnect()
}
