package com.suanmesgulum.app.tts

import android.util.Log
import java.io.File

/**
 * TTS Motor Yöneticisi.
 * Aktif TTS motorunu yönetir ve geçiş yapmayı sağlar.
 * Varsayılan olarak Android TTS kullanılır.
 * Kullanıcı premium paket satın alırsa Chatterbox'a geçiş yapılabilir.
 */
class TtsManager(
    private val androidTts: TtsEngine,
    private val chatterboxTts: TtsEngine
) {
    companion object {
        private const val TAG = "TtsManager"
    }

    /** Şu an aktif olan TTS motoru */
    private var activeEngine: TtsEngine = androidTts

    /** Premium özellik aktif mi */
    var isPremiumActive: Boolean = false
        set(value) {
            field = value
            activeEngine = if (value && chatterboxTts.isAvailable) {
                chatterboxTts
            } else {
                androidTts
            }
            Log.i(TAG, "Aktif TTS motoru: ${activeEngine.engineName}")
        }

    /** Aktif motorun adını döndürür */
    val activeEngineName: String
        get() = activeEngine.engineName

    /**
     * Aktif TTS motorunu başlat.
     */
    suspend fun initialize(): Boolean {
        val androidInitialized = androidTts.initialize()
        Log.i(TAG, "Android TTS başlatıldı: $androidInitialized")

        // Chatterbox'ı sadece premium aktifse başlat
        if (isPremiumActive) {
            val chatterboxInitialized = chatterboxTts.initialize()
            Log.i(TAG, "Chatterbox TTS başlatıldı: $chatterboxInitialized")
            if (chatterboxInitialized) {
                activeEngine = chatterboxTts
            }
        }

        return androidInitialized
    }

    /**
     * Metni sese çevir ve dosyaya kaydet.
     */
    suspend fun synthesizeToFile(text: String, outputFile: File): Boolean {
        return try {
            activeEngine.synthesizeToFile(text, outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Sentez hatası, yedek motora geçiliyor", e)
            // Chatterbox hata verirse Android TTS'e geri dön
            if (activeEngine != androidTts) {
                androidTts.synthesizeToFile(text, outputFile)
            } else {
                false
            }
        }
    }

    /**
     * Metni seslendir.
     */
    suspend fun speak(text: String): Boolean {
        return try {
            activeEngine.speak(text)
        } catch (e: Exception) {
            Log.e(TAG, "Konuşma hatası, yedek motora geçiliyor", e)
            if (activeEngine != androidTts) {
                androidTts.speak(text)
            } else {
                false
            }
        }
    }

    /**
     * Oynatmayı durdur.
     */
    fun stop() {
        activeEngine.stop()
    }

    /**
     * Tüm motorları kapat.
     */
    fun shutdown() {
        androidTts.shutdown()
        chatterboxTts.shutdown()
    }

    /**
     * Dili ayarla.
     */
    fun setLanguage(languageCode: String) {
        androidTts.setLanguage(languageCode)
        chatterboxTts.setLanguage(languageCode)
    }
}
