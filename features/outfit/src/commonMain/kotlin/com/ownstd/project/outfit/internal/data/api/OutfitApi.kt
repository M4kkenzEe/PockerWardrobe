package com.ownstd.project.outfit.internal.data.api

import com.ownstd.project.outfit.internal.data.dto.LookDto
import com.ownstd.project.outfit.internal.data.dto.ShareUrlDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OutfitApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getLooks(): List<LookDto> =
        client.get("$baseUrl$ENDPOINT") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun getLookById(id: Int): LookDto =
        client.get("$baseUrl$ENDPOINT/$id") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun deleteLook(id: Int) {
        client.delete("$baseUrl$ENDPOINT/$id") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun shareLook(id: Int): ShareUrlDto =
        client.post("$baseUrl$ENDPOINT/$id$SHARE") {
            contentType(ContentType.Application.Json)
        }.body()

    companion object {
        private const val ENDPOINT = "looks"
        private const val SHARE = "/share"
    }
}
