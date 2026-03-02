package com.suanmesgulum.app.domain.model

/**
 * Çağrı mesajı domain modeli.
 * Bir oturumdaki her konuşma parçası (asistanın söylediği veya arayanın söylediği).
 */
data class CallMessage(
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val speaker: Speaker,
    val messageText: String? = null,
    val audioFilePath: String? = null
)

/**
 * Konuşmacı türü.
 */
enum class Speaker {
    /** Asistan (TTS ile konuşan) */
    ASISTAN,
    /** Arayan kişi (STT ile metne dönüştürülen) */
    ARAYAN
}
