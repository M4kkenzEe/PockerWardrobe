package com.ownstd.project.network.internal

import com.ownstd.project.storage.SessionManager
import com.ownstd.project.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
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
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val userId: Long
)

// /refresh answers 401 for every dead token (invalid, expired, revoked, user gone) and never 400/403.
// Anything else (network failure, 429, 5xx) says nothing about the token, so the session must survive it.
private val refreshRejectedStatus = HttpStatusCode.Unauthorized

internal object NetworkClient {
    fun getClient(
        tokenStorage: TokenStorage,
        baseUrl: String,
        sessionManager: SessionManager,
        engine: HttpClientEngine? = null
    ): HttpClient {
        fun clearSessionAndNotify() {
            tokenStorage.clearSession()
            sessionManager.notifySessionExpired()
        }

        val configure: HttpClientConfig<*>.() -> Unit = {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.NONE
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
                                clearSessionAndNotify()
                                return@refreshTokens null
                            }
                        val response = try {
                            client.post("${baseUrl}refresh") {
                                markAsRefreshTokenRequest()
                                contentType(ContentType.Application.Json)
                                setBody(mapOf("refreshToken" to oldRefresh))
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            return@refreshTokens null
                        }
                        if (response.status == refreshRejectedStatus) {
                            clearSessionAndNotify()
                            return@refreshTokens null
                        }
                        if (response.status != HttpStatusCode.OK) return@refreshTokens null
                        val body = try {
                            response.body<RefreshResponse>()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            return@refreshTokens null
                        }
                        tokenStorage.saveSession(
                            accessToken  = body.accessToken,
                            refreshToken = body.refreshToken,
                            expiresAt    = body.expiresAt
                        )
                        BearerTokens(body.accessToken, body.refreshToken)
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

        return if (engine != null) HttpClient(engine, configure) else HttpClient(configure)
    }
}
