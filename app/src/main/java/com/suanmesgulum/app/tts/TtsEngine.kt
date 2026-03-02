package com.suanmesgulum.app.tts

import java.io.File

/**
 * TTS Motoru soyut arayüzü.
 * Farklı TTS motorları (Android yerleşik, Chatterbox vb.) bu arayüzü uygular.
 */
interface TtsEngine {

    /** Motor adı */
    val engineName: String

    /** Motor kullanılabilir mi? */
    val isAvailable: Boolean

    /** Motoru başlat */
    suspend fun initialize(): Boolean

    /** Metni sese çevirip dosyaya kaydet */
    suspend fun synthesizeToFile(text: String, outputFile: File): Boolean

    /** Metni doğrudan hoparlörden oynat */
    suspend fun speak(text: String): Boolean

    /** Ses oynatmayı durdur */
    fun stop()

    /** Kaynakları serbest bırak */
    fun shutdown()

    /** Dili ayarla (ISO 639 kodu, ör: "tr", "en") */
    fun setLanguage(languageCode: String)

    /** Konuşma hızını ayarla (1.0 = normal) */
    fun setSpeechRate(rate: Float)

    /** Ses ayarla (1.0 = normal) */
    fun setPitch(pitch: Float)
}
