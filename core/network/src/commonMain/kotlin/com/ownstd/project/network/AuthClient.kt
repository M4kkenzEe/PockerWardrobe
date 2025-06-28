package com.ownstd.project.network

import com.ownstd.project.network.model.LoginRequest
import com.ownstd.project.network.model.LoginResponse
import com.ownstd.project.network.model.RegisterRequest
import com.ownstd.project.network.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kotlinx.serialization.json.Json

class AuthClient(
    private val baseUrl: String
) : AuthService {
    private val client = HttpClient {
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
    }

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
            Result.failure(e)
        }
    }
}

