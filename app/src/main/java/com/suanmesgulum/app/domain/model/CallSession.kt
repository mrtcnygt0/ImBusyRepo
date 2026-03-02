package com.suanmesgulum.app.domain.model

/**
 * Çağrı oturumu domain modeli.
 * Asistanın cevapladığı her arama için bir oturum oluşturulur.
 */
data class CallSession(
    val id: Long = 0,
    val callLogId: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0,
    val recordingPath: String? = null,
    val transcript: String? = null,
    val status: SessionStatus = SessionStatus.DEVAM_EDIYOR
)

/**
 * Oturum durumu.
 */
enum class SessionStatus {
    /** Arama devam ediyor */
    DEVAM_EDIYOR,
    /** Arama normal şekilde tamamlandı */
    TAMAMLANDI,
    /** Asistan çağrıyı sonlandırdı */
    ASISTAN_TARAFINDAN_SONLANDIRILDI,
    /** Kullanıcı çağrıyı devraldı/cevapladı */
    KULLANICI_CEVAPLADI,
    /** Arayan kişi kapattı */
    ARAYAN_KAPATTI
}
