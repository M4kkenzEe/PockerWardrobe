package com.ownstd.project.wardrobe.internal.data.api

import com.ownstd.project.wardrobe.internal.data.dto.ClotheDto
import com.ownstd.project.wardrobe.internal.data.dto.ClotheUpdateRequest
import com.ownstd.project.wardrobe.internal.data.dto.LookDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.core.writeFully

class WardrobeApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getClothes(): List<ClotheDto> =
        client.get("$baseUrl$ENDPOINT") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun getClotheById(id: Int): ClotheDto =
        client.get("$baseUrl$ENDPOINT/$id") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun updateClothe(id: Int, body: ClotheUpdateRequest): ClotheDto =
        client.patch("$baseUrl$ENDPOINT/$id") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun uploadClothe(imageBytes: ByteArray): ClotheDto =
        client.post("$baseUrl$ENDPOINT") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("name", "")
                        append(
                            key = "image",
                            filename = "clothing_image.jpg",
                            contentType = ContentType.Image.JPEG,
                        ) {
                            writeFully(imageBytes)
                        }
                    }
                )
            )
        }.body()

    suspend fun uploadFromUrl(url: String): ClotheDto =
        client.get("$baseUrl$ENDPOINT$FROM_URL") {
            contentType(ContentType.Application.Json)
            this.url { parameters.append("url", url) }
        }.body()

    suspend fun deleteClothe(id: Int) {
        client.delete("$baseUrl$ENDPOINT/$id") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun getClotheOutfits(clotheId: Int): List<LookDto> =
        client.get("$baseUrl$ENDPOINT/$clotheId$OUTFITS") {
            contentType(ContentType.Application.Json)
        }.body()

    companion object {
        private const val ENDPOINT = "clothes"
        private const val FROM_URL = "/from_url"
        private const val OUTFITS = "/outfits"
    }
}
