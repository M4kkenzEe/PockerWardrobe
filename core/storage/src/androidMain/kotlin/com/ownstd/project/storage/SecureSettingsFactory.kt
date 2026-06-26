package com.ownstd.project.storage

import android.content.Context
import android.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings

private const val SECURE_PREFS_FILE = "secure_token_prefs"
private val MIGRATION_KEYS = listOf("session_access_token", "session_refresh_token", "session_expires_at")

internal actual fun createSecureSettings(): Settings {
    val context = SecureStorageContext.appContext
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    migrateFromPlaintext(context, encryptedPrefs)
    return SharedPreferencesSettings(encryptedPrefs)
}

@Suppress("DEPRECATION")
private fun migrateFromPlaintext(
    context: Context,
    encryptedPrefs: android.content.SharedPreferences
) {
    try {
        val plainPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val hasPlainTokens = MIGRATION_KEYS.any { plainPrefs.contains(it) }
        if (!hasPlainTokens) return

        val editor = encryptedPrefs.edit()
        MIGRATION_KEYS.forEach { key ->
            val value = plainPrefs.getString(key, null)
            if (value != null) editor.putString(key, value)
        }
        val expiresAt = plainPrefs.getLong("session_expires_at", -1L)
        if (expiresAt != -1L) editor.putLong("session_expires_at", expiresAt)
        editor.apply()

        plainPrefs.edit().apply {
            MIGRATION_KEYS.forEach { remove(it) }
            apply()
        }
    } catch (e: Exception) {
        // Migration failed — force re-login by clearing encrypted storage
        encryptedPrefs.edit().clear().apply()
    }
}
