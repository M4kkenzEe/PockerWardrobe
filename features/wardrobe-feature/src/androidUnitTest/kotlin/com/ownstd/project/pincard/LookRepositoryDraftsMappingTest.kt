package com.ownstd.project.pincard

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.data.model.DraftLookPreview
import com.ownstd.project.pincard.internal.data.model.GenerateDraftsResult
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import com.ownstd.project.pincard.internal.data.repository.LookRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

// Тела ответов повторяют DraftLooksResponse / NotEnoughForAnchorResponse / DraftLooksErrorResponse из LooksRoute.kt backend.
class LookRepositoryDraftsMappingTest {

    @Test fun `200 with looks maps to success`() = runTest {
        val result = generate(HttpStatusCode.OK, """{"looks":[{"lookId":456,"url":"http://x/a.png"},{"lookId":457,"url":""}],"allSaved":false}""")
        assertEquals(
            GenerateDraftsResult.Success(listOf(DraftLookPreview(456, "http://x/a.png"), DraftLookPreview(457, ""))),
            result,
        )
    }

    @Test fun `200 with allSaved maps to all saved`() = runTest {
        assertEquals(GenerateDraftsResult.AllSaved, generate(HttpStatusCode.OK, """{"looks":[],"allSaved":true}"""))
    }

    @Test fun `200 without looks and without allSaved is an error, not silence`() = runTest {
        assertEquals(GenerateDraftsResult.Error, generate(HttpStatusCode.OK, """{"looks":[],"allSaved":false}"""))
    }

    @Test fun `422 not enough for anchor keeps the missing categories`() = runTest {
        val result = generate(HttpStatusCode.UnprocessableEntity, """{"error":"not_enough_for_anchor","missing":["bottom","shoes"]}""")
        assertEquals(GenerateDraftsResult.NeedMore(listOf(MissingCategory.Bottom, MissingCategory.Shoes)), result)
    }

    @Test fun `422 not enough for anchor with only unknown categories is an error`() = runTest {
        val result = generate(HttpStatusCode.UnprocessableEntity, """{"error":"not_enough_for_anchor","missing":["hat"]}""")
        assertEquals(GenerateDraftsResult.Error, result)
    }

    @Test fun `422 unsupported category maps to unsupported`() = runTest {
        assertEquals(
            GenerateDraftsResult.Unsupported,
            generate(HttpStatusCode.UnprocessableEntity, """{"error":"unsupported_category"}"""),
        )
    }

    @Test fun `422 with unknown error is an error`() = runTest {
        assertEquals(GenerateDraftsResult.Error, generate(HttpStatusCode.UnprocessableEntity, """{"error":"something_new"}"""))
    }

    @Test fun `other statuses map to error`() = runTest {
        listOf(
            HttpStatusCode.BadRequest to """{"error":"invalid_request"}""",
            HttpStatusCode.NotFound to """{"error":"clothe_not_found"}""",
            HttpStatusCode.TooManyRequests to """{"error":"rate_limit"}""",
            HttpStatusCode.ServiceUnavailable to """{"error":"AI service unavailable"}""",
        ).forEach { (status, body) ->
            assertEquals(GenerateDraftsResult.Error, generate(status, body), "status $status")
        }
    }

    @Test fun `network failure maps to error`() = runTest {
        val engine = MockEngine { throw java.io.IOException("offline") }
        assertEquals(GenerateDraftsResult.Error, LookRepositoryImpl(networkOver(engine)).generateDrafts(7))
    }

    @Test fun `request posts the anchor clothe id to the drafts endpoint`() = runTest {
        val engine = MockEngine { respond("""{"looks":[],"allSaved":true}""", HttpStatusCode.OK, jsonHeaders) }
        LookRepositoryImpl(networkOver(engine)).generateDrafts(7)
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("http://test/api/v1/looks/drafts", request.url.toString())
        assertEquals("""{"anchorClotheId":7}""", request.body.toByteArray().decodeToString())
    }

    private suspend fun generate(status: HttpStatusCode, body: String): GenerateDraftsResult {
        val engine = MockEngine { respond(body, status, jsonHeaders) }
        return LookRepositoryImpl(networkOver(engine)).generateDrafts(7)
    }

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
