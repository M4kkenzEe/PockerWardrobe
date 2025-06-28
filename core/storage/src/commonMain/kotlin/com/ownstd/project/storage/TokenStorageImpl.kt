package com.ownstd.project.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set


class TokenStorageImpl(
    private val settings: Settings
) : TokenStorage {
    private companion object {
        const val TOKEN_KEY = "jwt_token"
    }

    override fun saveToken(token: String) {
        settings[TOKEN_KEY] = token
    }

    override fun getToken(): String? {
        return settings.getStringOrNull(TOKEN_KEY)
    }

    override fun clearToken() {
        settings.remove(TOKEN_KEY)
    }
}