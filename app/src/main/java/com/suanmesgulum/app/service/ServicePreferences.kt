package com.suanmesgulum.app.service

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Servis durumunu ve ayarları yöneten yardımcı sınıf.
 * SharedPreferences kullanarak servis durumunu saklar.
 */
class ServicePreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "suan_mesgulum_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_DEFAULT_MODE_ID = "default_mode_id"
        private const val KEY_AUTO_ANSWER = "auto_answer"
        private const val KEY_PREMIUM_PURCHASED = "premium_purchased"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_LAST_INCOMING_NUMBER = "last_incoming_number"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Servis aktif mi */
    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_SERVICE_ENABLED, value) }

    /** Varsayılan mod ID'si */
    var defaultModeId: Long
        get() = prefs.getLong(KEY_DEFAULT_MODE_ID, -1L)
        set(value) = prefs.edit { putLong(KEY_DEFAULT_MODE_ID, value) }

    /** Otomatik yanıtlama aktif mi */
    var isAutoAnswer: Boolean
        get() = prefs.getBoolean(KEY_AUTO_ANSWER, false)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_ANSWER, value) }

    /** Premium paket satın alındı mı */
    var isPremiumPurchased: Boolean
        get() = prefs.getBoolean(KEY_PREMIUM_PURCHASED, false)
        set(value) = prefs.edit { putBoolean(KEY_PREMIUM_PURCHASED, value) }

    /** Uygulama dili */
    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "tr") ?: "tr"
        set(value) = prefs.edit { putString(KEY_LANGUAGE, value) }

    /** Son gelen arama numarası (bildirim aksiyonlarında kullanılır) */
    var lastIncomingNumber: String
        get() = prefs.getString(KEY_LAST_INCOMING_NUMBER, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_INCOMING_NUMBER, value) }
}
