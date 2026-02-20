package com.ownstd.project.storage

interface TokenStorage {
    fun saveSession(accessToken: String, refreshToken: String, expiresAt: Long)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getExpiresAt(): Long?
    fun clearSession()
}
