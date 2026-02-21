package com.ownstd.project.network.internal

import com.ownstd.project.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val userId: Long
)

internal object NetworkClient {
    fun getClient(tokenStorage: TokenStorage, baseUrl: String): HttpClient {
        return HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val access = tokenStorage.getAccessToken() ?: return@loadTokens null
                        val refresh = tokenStorage.getRefreshToken() ?: return@loadTokens null
                        BearerTokens(access, refresh)
                    }
                    refreshTokens {
                        val oldRefresh = tokenStorage.getRefreshToken()
                            ?: run {
                                tokenStorage.clearSession()
                                return@refreshTokens null
                            }
                        try {
                            val response = client.post("${baseUrl}refresh") {
                                markAsRefreshTokenRequest()
                                contentType(ContentType.Application.Json)
                                setBody(mapOf("refreshToken" to oldRefresh))
                            }
                            if (response.status == HttpStatusCode.OK) {
                                val body = response.body<RefreshResponse>()
                                tokenStorage.saveSession(
                                    accessToken  = body.accessToken,
                                    refreshToken = body.refreshToken,
                                    expiresAt    = body.expiresAt
                                )
                                println("[Auth] token refreshed, userId=${body.userId}")
                                BearerTokens(body.accessToken, body.refreshToken)
                            } else {
                                println("[Auth] refresh failed: ${response.status}, clearing session")
                                tokenStorage.clearSession()
                                null
                            }
                        } catch (e: Exception) {
                            println("[Auth] refresh exception: ${e.message}, clearing session")
                            tokenStorage.clearSession()
                            null
                        }
                    }
                    sendWithoutRequest { request ->
                        val url = request.url.toString()
                        !url.endsWith("login") &&
                        !url.endsWith("register") &&
                        !url.endsWith("refresh")
                    }
                }
            }
        }
    }
}
