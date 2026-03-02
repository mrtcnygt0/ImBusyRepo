package com.suanmesgulum.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Asistan ayarları entity'si.
 * Tek bir satırlık tablo (id = 1).
 * Asistanın adı, kişiliği, varsayılan dil, ses ve mesaj ayarları burada saklanır.
 */
@Entity(tableName = "assistant_settings")
data class AssistantSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val assistantName: String = "Asistan",
    val personality: String = "Resmi",
    val defaultGreetingMessageId: Long = -1,
    val defaultVoiceId: String = "default",
    val defaultLanguage: String = "tr",
    val spotifyConnected: Boolean = false,
    val spotifyPlaylistId: String? = null,
    val infoGatheringMessage: String = "Size nasıl yardımcı olabilirim?",
    val farewellMessage: String = "İlginiz için teşekkürler, iyi günler.",
    val voicemailPromptMessage: String = "Bir mesaj bırakmak ister misiniz? Lütfen mesajınızı bıptan sonra söyleyin."
)
