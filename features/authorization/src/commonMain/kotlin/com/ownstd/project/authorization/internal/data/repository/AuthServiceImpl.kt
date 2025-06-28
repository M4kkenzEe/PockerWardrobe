package com.ownstd.project.authorization.internal.data.repository

import com.ownstd.project.authorization.internal.data.model.LoginRequest
import com.ownstd.project.authorization.internal.data.model.LoginResponse
import com.ownstd.project.authorization.internal.data.model.RegisterRequest
import com.ownstd.project.authorization.internal.data.model.User
import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.network.api.NetworkRepository
import io.ktor.client.call.body
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

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        gender: String
    ): Result<User> {
        return try {
            val response = client.post("$baseUrl/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password, gender))
            }
            when (response.status) {
                HttpStatusCode.Created -> Result.success(response.body<User>())
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
    ): Result<String> {
        return try {
            val response = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(response.body<LoginResponse>().token)
                HttpStatusCode.Unauthorized -> Result.failure(Exception("Invalid credentials"))
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Login failed: ${e.message}")
            Result.failure(e)
        }
    }
}
