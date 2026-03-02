package com.suanmesgulum.app.domain.model

/**
 * Asistan ayarları domain modeli.
 * Tek bir satırlık tablo olarak saklanır (id = 1).
 */
data class AssistantSettings(
    val id: Int = 1,
    val assistantName: String = "Asistan",
    val personality: String = "Resmi",
    val defaultGreetingMessageId: Long = -1,
    val defaultVoiceId: String = "default",
    val defaultLanguage: String = "tr",
    val spotifyConnected: Boolean = false,
    val spotifyPlaylistId: String? = null,
    /** Bilgi alma mesaj metni */
    val infoGatheringMessage: String = "Size nasıl yardımcı olabilirim?",
    /** Veda/red mesaj metni */
    val farewellMessage: String = "İlginiz için teşekkürler, iyi günler.",
    /** Sesli mesaj bırakma mesajı */
    val voicemailPromptMessage: String = "Bir mesaj bırakmak ister misiniz? Lütfen mesajınızı bıptan sonra söyleyin."
)
