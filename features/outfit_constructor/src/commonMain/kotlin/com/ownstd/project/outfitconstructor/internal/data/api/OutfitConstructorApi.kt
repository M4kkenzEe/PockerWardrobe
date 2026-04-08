package com.ownstd.project.outfitconstructor.internal.data.api

import com.ownstd.project.outfitconstructor.internal.data.dto.AddLookRequest
import com.ownstd.project.outfitconstructor.internal.data.dto.ClotheDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.core.writeFully

class OutfitConstructorApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getClothes(): List<ClotheDto> =
        client.get("$baseUrl$CLOTHES_ENDPOINT") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun addLook(request: AddLookRequest, image: ByteArray) {
        client.post("$baseUrl$LOOKS_ENDPOINT") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("name", request.name)
                        append("clothe_ids", request.clotheIds.joinToString(","))
                        append(
                            key = "image",
                            filename = "look_image.jpg",
                            contentType = ContentType.Image.JPEG,
                        ) {
                            writeFully(image)
                        }
                    }
                )
            )
        }
    }

    companion object {
        private const val CLOTHES_ENDPOINT = "clothes"
        private const val LOOKS_ENDPOINT = "looks"
    }
}
