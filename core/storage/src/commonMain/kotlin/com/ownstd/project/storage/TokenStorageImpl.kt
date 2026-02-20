package com.ownstd.project.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set


internal class TokenStorageImpl(
    private val settings: Settings
) : TokenStorage {
    private companion object {
        const val KEY_ACCESS_TOKEN  = "session_access_token"
        const val KEY_REFRESH_TOKEN = "session_refresh_token"
        const val KEY_EXPIRES_AT    = "session_expires_at"
    }

    override fun saveSession(accessToken: String, refreshToken: String, expiresAt: Long) {
        settings[KEY_ACCESS_TOKEN]  = accessToken
        settings[KEY_REFRESH_TOKEN] = refreshToken
        settings[KEY_EXPIRES_AT]    = expiresAt
        println("[TokenStorage] session saved, expiresAt=$expiresAt")
    }

    override fun getAccessToken(): String? = settings.getStringOrNull(KEY_ACCESS_TOKEN)

    override fun getRefreshToken(): String? = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    override fun getExpiresAt(): Long? =
        if (settings.hasKey(KEY_EXPIRES_AT)) settings.getLong(KEY_EXPIRES_AT, 0L) else null

    override fun clearSession() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_EXPIRES_AT)
        println("[TokenStorage] session cleared")
    }
}
