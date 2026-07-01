package com.ownstd.project.authorization.internal.data.repository

import com.ownstd.project.authorization.internal.data.model.AuthTokenResponse
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordRequest
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordResponse
import com.ownstd.project.authorization.internal.data.model.LoginRequest
import com.ownstd.project.authorization.internal.data.model.RegisterRequest
import com.ownstd.project.authorization.internal.data.model.ResetPasswordRequest
import com.ownstd.project.authorization.internal.data.model.TelegramInitResponse
import com.ownstd.project.authorization.internal.data.model.TelegramStatusResponse
import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.network.api.NetworkRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class AuthServiceImpl(
    private val networkRepository: NetworkRepository
) : AuthService {

    private val client = networkRepository.getClient()
    private val baseUrl = networkRepository.baseUrl

    init {
        println("🔧 AuthServiceImpl initialized with baseUrl: $baseUrl")
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        gender: String
    ): Result<AuthTokenResponse> {
        return try {
            val response = client.post(baseUrl + REGISTER_URL) {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password, gender))
            }
            when (response.status) {
                HttpStatusCode.Created -> Result.success(response.body<AuthTokenResponse>())
                HttpStatusCode.Conflict -> Result.failure(Exception("User with this username or email already exists"))
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(
        username: String,
        password: String
    ): Result<AuthTokenResponse> {
        return try {
            val fullUrl = baseUrl + LOGIN_URL
            println("🔧 AuthService.login - attempting login with URL: $fullUrl")
            val response = client.post(fullUrl) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(login = username, password = password))
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(response.body<AuthTokenResponse>())
                HttpStatusCode.Unauthorized -> Result.failure(Exception("Invalid credentials"))
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        return try {
            val response = client.post(baseUrl + FORGOT_PASSWORD_URL) {
                contentType(ContentType.Application.Json)
                setBody(ForgotPasswordRequest(email))
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(response.body<ForgotPasswordResponse>())
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): Result<AuthTokenResponse> {
        return try {
            val response = client.post(baseUrl + RESET_PASSWORD_URL) {
                contentType(ContentType.Application.Json)
                setBody(ResetPasswordRequest(email, code, newPassword))
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(response.body<AuthTokenResponse>())
                HttpStatusCode.BadRequest -> Result.failure(Exception("Неверный код или email"))
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTelegramInit(): Result<TelegramInitResponse> {
        return try {
            val response = client.get(baseUrl + TELEGRAM_INIT_URL)
            when (response.status) {
                HttpStatusCode.OK -> Result.success(response.body<TelegramInitResponse>())
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTelegramStatus(state: String): Result<TelegramStatusResponse> {
        return try {
            val response = client.get("$baseUrl$TELEGRAM_STATUS_URL?state=$state")
            when (response.status) {
                HttpStatusCode.OK -> Result.success(response.body<TelegramStatusResponse>())
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val REGISTER_URL = "register"
        const val LOGIN_URL = "login"
        const val FORGOT_PASSWORD_URL = "auth/forgot-password"
        const val RESET_PASSWORD_URL = "auth/reset-password"
        const val TELEGRAM_INIT_URL = "auth/telegram/init"
        const val TELEGRAM_STATUS_URL = "auth/telegram/status"
    }
}
