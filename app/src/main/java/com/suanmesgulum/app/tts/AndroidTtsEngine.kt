package com.suanmesgulum.app.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android yerleşik TTS motorunu kullanan implementasyon.
 * Ücretsiz versiyonda bu motor kullanılır.
 * Cihazda Google TTS veya Pico TTS yüklüyse çevrimdışı çalışır.
 */
class AndroidTtsEngine(
    private val context: Context
) : TtsEngine {

    companion object {
        private const val TAG = "AndroidTtsEngine"
    }

    override val engineName: String = "Android TTS"

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    override val isAvailable: Boolean
        get() = isInitialized

    override suspend fun initialize(): Boolean = suspendCoroutine { continuation ->
        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            if (isInitialized) {
                // Varsayılan olarak Türkçe dil ayarla
                val result = tts?.setLanguage(Locale("tr", "TR"))
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.w(TAG, "Türkçe TTS dil verisi eksik, İngilizce kullanılacak")
                    tts?.setLanguage(Locale.US)
                }
                Log.i(TAG, "Android TTS başarıyla başlatıldı")
            } else {
                Log.e(TAG, "Android TTS başlatılamadı")
            }
            continuation.resume(isInitialized)
        }
    }

    override suspend fun synthesizeToFile(text: String, outputFile: File): Boolean =
        suspendCoroutine { continuation ->
            if (!isInitialized) {
                Log.e(TAG, "TTS henüz başlatılmadı")
                continuation.resume(false)
                return@suspendCoroutine
            }

            val utteranceId = UUID.randomUUID().toString()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {
                    Log.d(TAG, "Sentez başladı: $id")
                }

                override fun onDone(id: String?) {
                    Log.d(TAG, "Sentez tamamlandı: $id")
                    if (id == utteranceId) {
                        continuation.resume(true)
                    }
                }

                @Deprecated("Deprecated in API")
                override fun onError(id: String?) {
                    Log.e(TAG, "Sentez hatası: $id")
                    if (id == utteranceId) {
                        continuation.resume(false)
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e(TAG, "Sentez hatası: $utteranceId, kod: $errorCode")
                    continuation.resume(false)
                }
            })

            // Paralinguistik etiketleri temizle (ilk versiyonda desteklenmiyor)
            val cleanText = cleanParalinguisticTags(text)

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            val result = tts?.synthesizeToFile(cleanText, params, outputFile, utteranceId)
            if (result != TextToSpeech.SUCCESS) {
                Log.e(TAG, "synthesizeToFile başlatılamadı")
                continuation.resume(false)
            }
        }

    override suspend fun speak(text: String): Boolean = suspendCoroutine { continuation ->
        if (!isInitialized) {
            Log.e(TAG, "TTS henüz başlatılmadı")
            continuation.resume(false)
            return@suspendCoroutine
        }

        val utteranceId = UUID.randomUUID().toString()

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                Log.d(TAG, "Konuşma başladı: $id")
            }

            override fun onDone(id: String?) {
                Log.d(TAG, "Konuşma tamamlandı: $id")
                if (id == utteranceId) {
                    continuation.resume(true)
                }
            }

            @Deprecated("Deprecated in API")
            override fun onError(id: String?) {
                Log.e(TAG, "Konuşma hatası: $id")
                if (id == utteranceId) {
                    continuation.resume(false)
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(TAG, "Konuşma hatası: $utteranceId, kod: $errorCode")
                continuation.resume(false)
            }
        })

        val cleanText = cleanParalinguisticTags(text)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            // Ses akışını telefon araması olarak ayarla
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_VOICE_CALL)
        }

        val result = tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            Log.e(TAG, "speak başlatılamadı")
            continuation.resume(false)
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    override fun setLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "tr" -> Locale("tr", "TR")
            "en" -> Locale.US
            else -> Locale(languageCode)
        }
        tts?.setLanguage(locale)
    }

    override fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    override fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    /**
     * Paralinguistik etiketleri temizle ([laugh], [cough] vb.)
     * İlk versiyonda bu etiketler görmezden gelinir.
     */
    private fun cleanParalinguisticTags(text: String): String {
        return text.replace(Regex("\\[\\w+\\]"), "").trim()
    }
}
