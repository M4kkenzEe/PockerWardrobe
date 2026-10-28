package com.ownstd.project.authorization.internal.data.repository

import com.ownstd.project.authorization.internal.data.model.AuthTokenResponse
import com.ownstd.project.authorization.internal.data.model.ErrorResponse
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordRequest
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordResponse
import com.ownstd.project.authorization.internal.data.model.LoginRequest
import com.ownstd.project.authorization.internal.data.model.LogoutRequest
import com.ownstd.project.authorization.internal.data.model.RegisterRequest
import com.ownstd.project.authorization.internal.data.model.ResetPasswordRequest
import com.ownstd.project.authorization.internal.data.model.StartRegistrationRequest
import com.ownstd.project.authorization.internal.data.model.VerifyRegistrationRequest
import com.ownstd.project.authorization.internal.data.model.VerifyRegistrationResponse
import com.ownstd.project.authorization.internal.data.model.VerifyEmailRequest
import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.authorization.internal.domain.BAD_REQUEST_MESSAGE
import com.ownstd.project.authorization.internal.domain.EMAIL_ALREADY_REGISTERED_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_EMAIL_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_CREDENTIALS_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_OR_EXPIRED_CODE_MESSAGE
import com.ownstd.project.authorization.internal.domain.NETWORK_ERROR_MESSAGE
import com.ownstd.project.authorization.internal.domain.REGISTRATION_REJECTED_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESEND_RATE_LIMIT_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESET_CODE_EXPIRED_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESET_INVALID_CODE_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESET_INVALID_PASSWORD_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESET_REJECTED_MESSAGE
import com.ownstd.project.authorization.internal.domain.SERVER_ERROR_MESSAGE
import com.ownstd.project.authorization.internal.domain.USER_ALREADY_EXISTS_MESSAGE
import com.ownstd.project.network.api.NetworkRepository
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException

class AuthServiceImpl(
    private val networkRepository: NetworkRepository
) : AuthService {

    private val client = networkRepository.getClient()
    private val baseUrl = networkRepository.baseUrl

    init {
        println("🔧 AuthServiceImpl initialized with baseUrl: $baseUrl")
    }

    override suspend fun register(
        username: String?,
        email: String,
        password: String,
        gender: String,
        verificationToken: String
    ): Result<AuthTokenResponse> {
        val response = send {
            client.post(baseUrl + REGISTER_URL) {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password, gender, verificationToken))
            }
        }.getOrElse { return Result.failure(it) }
        return when (response.status) {
            HttpStatusCode.Created -> runCatching { response.body<AuthTokenResponse>() }
            HttpStatusCode.Conflict -> Result.failure(Exception(USER_ALREADY_EXISTS_MESSAGE))
            HttpStatusCode.BadRequest -> Result.failure(Exception(REGISTRATION_REJECTED_MESSAGE))
            else -> Result.failure(Exception(statusFailure(response.status)))
        }
    }

    override suspend fun login(
        username: String,
        password: String
    ): Result<AuthTokenResponse> {
        val response = send {
            client.post(baseUrl + LOGIN_URL) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(login = username, password = password))
            }
        }.getOrElse { return Result.failure(it) }
        return when (response.status) {
            HttpStatusCode.OK -> runCatching { response.body<AuthTokenResponse>() }
            HttpStatusCode.Unauthorized -> Result.failure(Exception(INVALID_CREDENTIALS_MESSAGE))
            HttpStatusCode.BadRequest -> Result.failure(Exception(BAD_REQUEST_MESSAGE))
            else -> Result.failure(Exception(statusFailure(response.status)))
        }
    }

    override suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        val response = try {
            client.post(baseUrl + FORGOT_PASSWORD_URL) {
                contentType(ContentType.Application.Json)
                setBody(ForgotPasswordRequest(email))
            }
        } catch (e: Exception) {
            return Result.failure(Exception(NETWORK_ERROR_MESSAGE, e))
        }
        if (response.status != HttpStatusCode.OK) return Result.failure(Exception(statusFailure(response.status)))
        return runCatching { response.body<ForgotPasswordResponse>() }
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): Result<AuthTokenResponse> {
        val response = try {
            client.post(baseUrl + RESET_PASSWORD_URL) {
                contentType(ContentType.Application.Json)
                setBody(ResetPasswordRequest(email, code, newPassword))
            }
        } catch (e: Exception) {
            return Result.failure(Exception(NETWORK_ERROR_MESSAGE, e))
        }
        if (response.status != HttpStatusCode.OK) return Result.failure(Exception(resetPasswordFailure(response)))
        return runCatching { response.body<AuthTokenResponse>() }
    }

    private suspend fun resetPasswordFailure(response: HttpResponse): String =
        if (response.status == HttpStatusCode.BadRequest) resetErrorCodeFailure(response.errorCode())
        else statusFailure(response.status)

    private fun statusFailure(status: HttpStatusCode): String = when {
        status == HttpStatusCode.TooManyRequests -> RESEND_RATE_LIMIT_MESSAGE
        status.value in 500..599 -> SERVER_ERROR_MESSAGE
        else -> "Unexpected response: $status"
    }

    private fun resetErrorCodeFailure(errorCode: String?): String = when (errorCode) {
        "INVALID_CODE" -> RESET_INVALID_CODE_MESSAGE
        "CODE_EXPIRED" -> RESET_CODE_EXPIRED_MESSAGE
        "INVALID_PASSWORD" -> RESET_INVALID_PASSWORD_MESSAGE
        else -> RESET_REJECTED_MESSAGE
    }

    private suspend fun HttpResponse.errorCode(): String? = runCatching { body<ErrorResponse>().error }.getOrNull()

    override suspend fun logout(refreshToken: String): Result<Unit> {
        return try {
            val response = client.post(baseUrl + LOGOUT_URL) {
                contentType(ContentType.Application.Json)
                setBody(LogoutRequest(refreshToken))
            }
            // The backend answers 204 to /logout.
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Unexpected response: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyEmail(code: String): Result<Unit> {
        return try {
            val response = client.post(baseUrl + VERIFY_EMAIL_URL) {
                contentType(ContentType.Application.Json)
                setBody(VerifyEmailRequest(code))
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(Unit)
                HttpStatusCode.BadRequest,
                HttpStatusCode.Gone -> Result.failure(Exception(INVALID_OR_EXPIRED_CODE_MESSAGE))
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resendVerification(): Result<Unit> {
        return try {
            val response = client.post(baseUrl + RESEND_VERIFICATION_URL) {
                contentType(ContentType.Application.Json)
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(Unit)
                HttpStatusCode.TooManyRequests -> Result.failure(Exception(RESEND_RATE_LIMIT_MESSAGE))
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestRegistrationCode(email: String): Result<Unit> {
        val response = send {
            client.post(baseUrl + REGISTRATION_START_URL) {
                contentType(ContentType.Application.Json)
                setBody(StartRegistrationRequest(email))
            }
        }.getOrElse { return Result.failure(it) }
        return when (response.status) {
            HttpStatusCode.OK -> Result.success(Unit)
            HttpStatusCode.BadRequest -> Result.failure(Exception(INVALID_EMAIL_MESSAGE))
            HttpStatusCode.Conflict -> Result.failure(Exception(EMAIL_ALREADY_REGISTERED_MESSAGE))
            else -> Result.failure(Exception(statusFailure(response.status)))
        }
    }

    override suspend fun verifyRegistrationCode(email: String, code: String): Result<String> {
        val response = send {
            client.post(baseUrl + REGISTRATION_VERIFY_URL) {
                contentType(ContentType.Application.Json)
                setBody(VerifyRegistrationRequest(email, code))
            }
        }.getOrElse { return Result.failure(it) }
        return when (response.status) {
            HttpStatusCode.OK -> runCatching { response.body<VerifyRegistrationResponse>().verificationToken }
            HttpStatusCode.BadRequest -> Result.failure(Exception(INVALID_OR_EXPIRED_CODE_MESSAGE))
            else -> Result.failure(Exception(statusFailure(response.status)))
        }
    }

    private suspend fun send(request: suspend () -> HttpResponse): Result<HttpResponse> = try {
        Result.success(request())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(Exception(NETWORK_ERROR_MESSAGE, e))
    }

    companion object {
        const val REGISTER_URL = "register"
        const val LOGIN_URL = "login"
        const val LOGOUT_URL = "logout"
        const val FORGOT_PASSWORD_URL = "auth/forgot-password"
        const val RESET_PASSWORD_URL = "auth/reset-password"
        const val VERIFY_EMAIL_URL = "auth/verify-email"
        const val RESEND_VERIFICATION_URL = "auth/resend-verification"
        const val REGISTRATION_START_URL = "auth/registration/start"
        const val REGISTRATION_VERIFY_URL = "auth/registration/verify"
    }
}
