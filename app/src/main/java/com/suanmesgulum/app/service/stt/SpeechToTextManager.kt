package com.suanmesgulum.app.service.stt

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vosk tabanlı Speech-to-Text yöneticisi.
 *
 * Cihaz içinde çalışan açık kaynak STT motoru.
 * Sürekli dinleme modunda çalışabilir ve kısmi sonuçlar verebilir.
 *
 * Kullanım:
 * 1. initialize() — Vosk modelini yükle
 * 2. startListening() — Ses verisi almaya ve metne çevirmeye başla
 * 3. feedAudioData() — Ses verisi besle (AudioRecord'dan gelen byte[])
 * 4. stopListening() — Dinlemeyi durdur
 *
 * Model dosyaları assets/model dizinine yerleştirilir veya
 * ilk açılışta indirilir.
 */
@Singleton
class SpeechToTextManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "SpeechToTextManager"
        private const val SAMPLE_RATE = 16000f
        // Vosk Türkçe model adı
        private const val MODEL_NAME = "vosk-model-small-tr-0.3"
    }

    /** Vosk model */
    private var model: Model? = null
    private var recognizer: Recognizer? = null

    /** Model yüklendi mi? */
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    /** Model yükleniyor mu? */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Kısmi tanıma sonuçları (gerçek zamanlı) */
    private val _partialResult = MutableSharedFlow<String>(replay = 0)
    val partialResult: SharedFlow<String> = _partialResult.asSharedFlow()

    /** Tamamlanmış tanıma sonuçları */
    private val _finalResult = MutableSharedFlow<String>(replay = 0)
    val finalResult: SharedFlow<String> = _finalResult.asSharedFlow()

    /** Dinleme durumu */
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    /**
     * Vosk modelini başlat.
     * Model assets'ten veya indirilen dizinden yüklenir.
     * @return true model başarıyla yüklendi
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (_isModelLoaded.value) {
            Log.d(TAG, "Model zaten yüklü")
            return@withContext true
        }

        _isLoading.value = true
        try {
            // Önce uygulamanın files dizininde model var mı kontrol et
            val modelDir = File(context.filesDir, "vosk-model")
            if (modelDir.exists() && modelDir.isDirectory) {
                model = Model(modelDir.absolutePath)
                _isModelLoaded.value = true
                Log.i(TAG, "Vosk modeli dosya sisteminden yüklendi")
                return@withContext true
            }

            // Assets'ten model yüklemeyi dene
            try {
                StorageService.unpack(context, "model", "vosk-model",
                    { loadedModel ->
                        model = loadedModel
                        _isModelLoaded.value = true
                        Log.i(TAG, "Vosk modeli assets'ten yüklendi")
                    },
                    { exception ->
                        Log.e(TAG, "Vosk model yükleme hatası", exception)
                    }
                )
                // StorageService asenkron çalışır, başlatıldı demek
                // Model callback ile yüklenecek
                return@withContext true
            } catch (e: Exception) {
                Log.w(TAG, "Assets'te Vosk modeli bulunamadı: ${e.message}")
                Log.i(TAG, "Vosk modeli henüz indirilmemiş. Lütfen modeli indirin.")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vosk başlatma hatası", e)
            return@withContext false
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Dinlemeye başla.
     * Recognizer oluşturulur ve feedAudioData() ile beslenir.
     */
    fun startListening() {
        val currentModel = model
        if (currentModel == null) {
            Log.e(TAG, "Model yüklenmeden dinleme başlatılamaz")
            return
        }

        try {
            recognizer?.close()
            recognizer = Recognizer(currentModel, SAMPLE_RATE)
            _isListening.value = true
            Log.i(TAG, "Dinleme başlatıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Recognizer oluşturma hatası", e)
        }
    }

    /**
     * Ses verisi besle (AudioRecord'dan gelen byte dizisi).
     * Bu metot, kısmi ve tamamlanmış sonuçları SharedFlow aracılığıyla yayar.
     *
     * @param audioData Ses verisi (PCM 16-bit, 16kHz, mono)
     * @param length Veri uzunluğu
     */
    suspend fun feedAudioData(audioData: ByteArray, length: Int) {
        val rec = recognizer ?: return
        if (!_isListening.value) return

        try {
            if (rec.acceptWaveForm(audioData, length)) {
                // Tamamlanmış sonuç
                val result = rec.result
                val text = parseVoskResult(result)
                if (text.isNotBlank()) {
                    _finalResult.emit(text)
                    Log.d(TAG, "Final sonuç: $text")
                }
            } else {
                // Kısmi sonuç
                val partial = rec.partialResult
                val text = parseVoskPartialResult(partial)
                if (text.isNotBlank()) {
                    _partialResult.emit(text)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ses verisi işleme hatası", e)
        }
    }

    /**
     * Dinlemeyi durdur ve son sonucu al.
     * @return Son tamamlanmış metin
     */
    suspend fun stopListening(): String {
        _isListening.value = false
        val rec = recognizer ?: return ""

        return try {
            val finalResult = rec.finalResult
            val text = parseVoskResult(finalResult)
            if (text.isNotBlank()) {
                _finalResult.emit(text)
            }
            rec.close()
            recognizer = null
            Log.i(TAG, "Dinleme durduruldu")
            text
        } catch (e: Exception) {
            Log.e(TAG, "Dinleme durdurma hatası", e)
            ""
        }
    }

    /**
     * Vosk JSON sonucunu parse et.
     * Örnek: {"text": "merhaba nasılsınız"}
     */
    private fun parseVoskResult(json: String): String {
        return try {
            JSONObject(json).optString("text", "")
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Vosk kısmi (partial) JSON sonucunu parse et.
     * Örnek: {"partial": "merhaba nasıl"}
     */
    private fun parseVoskPartialResult(json: String): String {
        return try {
            JSONObject(json).optString("partial", "")
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Vosk modelinin indirilip yüklenip yüklenmediğini kontrol et.
     */
    fun isModelAvailable(): Boolean {
        return _isModelLoaded.value || File(context.filesDir, "vosk-model").exists()
    }

    /**
     * Kaynakları serbest bırak.
     */
    fun shutdown() {
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
        _isModelLoaded.value = false
        _isListening.value = false
        Log.i(TAG, "SpeechToTextManager kapatıldı")
    }
}
