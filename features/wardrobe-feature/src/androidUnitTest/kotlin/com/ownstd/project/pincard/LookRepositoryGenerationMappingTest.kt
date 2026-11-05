package com.ownstd.project.pincard

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.data.model.GenerateLooksResult
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import com.ownstd.project.pincard.internal.data.repository.LookRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*

class LookRepositoryGenerationMappingTest {
    @Test fun `missing categories reach the caller in canonical order without duplicates`() = runTest {
        assertEquals(GenerateLooksResult.NotEnoughClothes(listOf(MissingCategory.Bottom, MissingCategory.Shoes)),
            generate(HttpStatusCode.BadRequest, """{"error":"not_enough_clothes","missing":["shoes","bottom","shoes"],"extra":true}"""))
    }
    @Test fun `each category survives the response`() = runTest {
        listOf("top" to MissingCategory.Top, "bottom" to MissingCategory.Bottom, "shoes" to MissingCategory.Shoes).forEach { (raw, category) ->
            assertEquals(GenerateLooksResult.NotEnoughClothes(listOf(category)),
                generate(HttpStatusCode.BadRequest, """{"error":"not_enough_clothes","missing":["$raw"]}"""))
        }
    }
    @Test fun `legacy empty and unknown missing use generic feedback`() = runTest {
        listOf("", ",\"missing\":[]", ",\"missing\":[\"hat\",\"top\"]").forEach { missing ->
            assertEquals(GenerateLooksResult.NotEnoughClothes(), generate(HttpStatusCode.BadRequest, """{"error":"not_enough_clothes"$missing}"""))
        }
    }
    @Test fun `other bad requests and server errors are not wardrobe feedback`() = runTest {
        assertIs<GenerateLooksResult.NetworkError>(generate(HttpStatusCode.BadRequest, """{"error":"user_not_found"}"""))
        assertIs<GenerateLooksResult.NetworkError>(generate(HttpStatusCode.InternalServerError, "{}"))
    }
    @Test fun `network failures are separate`() = runTest {
        val engine = MockEngine { throw java.io.IOException("offline") }
        assertIs<GenerateLooksResult.NetworkError>(LookRepositoryImpl(networkOver(engine)).generateLooks())
    }
    @Test fun `empty success stays success and two item outfit is accepted`() = runTest {
        assertEquals(GenerateLooksResult.Success(emptyList()), generate(HttpStatusCode.OK, """{"look_ids":[]}"""))
        assertEquals(GenerateLooksResult.Success(listOf(7)), generate(HttpStatusCode.OK, """{"look_ids":[7]}"""))
    }
    private suspend fun generate(status: HttpStatusCode, body: String): GenerateLooksResult =
        LookRepositoryImpl(networkOver(MockEngine { respond(body, status, jsonHeaders) })).generateLooks()

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun networkOver(engine: MockEngine) = object : NetworkRepository {
        override val baseUrl = "http://test/api/v1/"
        private val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        override fun getClient() = client
        override fun clearAuthCache() = Unit
    }
}
