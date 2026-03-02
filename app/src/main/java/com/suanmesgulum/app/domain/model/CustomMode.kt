package com.suanmesgulum.app.domain.model

/**
 * Kullanıcının tanımladığı meşgul modu.
 * Gelen aramada bu mod seçildiğinde, text TTS ile okunur.
 */
data class CustomMode(
    val id: Long = 0,
    val name: String,
    val text: String,
    val orderIndex: Int = 0,
    val isDefault: Boolean = false
)
