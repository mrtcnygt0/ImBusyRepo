package com.suanmesgulum.app.service.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Geliştirilmiş Ses Oynatma Yöneticisi.
 *
 * TTS çıktısını ve gelecekte müzik (Spotify) çalmayı yönetir.
 * Ses kaynağını (TTS mi, müzik mi) ve ses seviyesini kontrol eder.
 * Çağrı sırasında doğru ses kanallarını kullanır.
 */
@Singleton
class AudioPlayerManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "AudioPlayerManager"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var musicPlayer: MusicPlayer? = null

    /** Şu anda çalan kaynak türü */
    enum class AudioSource { TTS, MUSIC, NONE }
    var currentSource: AudioSource = AudioSource.NONE
        private set

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * Müzik oynatıcısını ayarla (ileride Spotify için).
     */
    fun setMusicPlayer(player: MusicPlayer) {
        musicPlayer = player
    }

    /**
     * TTS ses dosyasını çağrı kanalı üzerinden oynat.
     * @param audioFile Oynatılacak ses dosyası
     * @return true oynatma başarılı
     */
    suspend fun playTtsAudio(audioFile: File): Boolean = suspendCoroutine { continuation ->
        try {
            currentSource = AudioSource.TTS
            releaseMediaPlayer()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .build()
                )
                setDataSource(audioFile.absolutePath)
                prepare()

                setOnCompletionListener {
                    currentSource = AudioSource.NONE
                    Log.d(TAG, "TTS oynatma tamamlandı")
                    continuation.resume(true)
                }

                setOnErrorListener { _, what, extra ->
                    currentSource = AudioSource.NONE
                    Log.e(TAG, "MediaPlayer hatası: what=$what, extra=$extra")
                    continuation.resume(false)
                    true
                }

                start()
            }
            Log.i(TAG, "TTS ses oynatma başladı")
        } catch (e: Exception) {
            currentSource = AudioSource.NONE
            Log.e(TAG, "TTS oynatma hatası", e)
            continuation.resume(false)
        }
    }

    /**
     * Bekletme müziği çal (ileride Spotify entegrasyonu ile).
     * Şu anda stub — loglar ve false döndürür.
     */
    suspend fun playHoldMusic(trackId: String): Boolean {
        val player = musicPlayer
        if (player == null) {
            Log.w(TAG, "Müzik oynatıcısı ayarlanmamış (Spotify entegrasyonu ileride)")
            return false
        }

        currentSource = AudioSource.MUSIC
        return try {
            player.play(trackId)
        } catch (e: Exception) {
            Log.e(TAG, "Müzik oynatma hatası", e)
            false
        }
    }

    /**
     * Ses odağı talep et.
     */
    fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioManager?.requestAudioFocus(audioFocusRequest!!)
        }
    }

    /**
     * Ses odağını serbest bırak.
     */
    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            }
        }
    }

    /**
     * Ses modunu ayarla.
     */
    fun setAudioMode(mode: Int) {
        audioManager?.mode = mode
    }

    /**
     * Oynatmayı durdur.
     */
    fun stop() {
        releaseMediaPlayer()
        musicPlayer?.stop()
        currentSource = AudioSource.NONE
    }

    /**
     * MediaPlayer'ı serbest bırak.
     */
    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer release hatası", e)
        }
        mediaPlayer = null
    }

    /**
     * Tüm kaynakları serbest bırak.
     */
    fun shutdown() {
        releaseMediaPlayer()
        abandonAudioFocus()
        musicPlayer?.disconnect()
        currentSource = AudioSource.NONE
    }
}
