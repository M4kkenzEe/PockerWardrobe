package com.ownstd.project.outfit.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LookDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("url") val url: String,
    @SerialName("look_items") val lookItems: List<LookItemDto>? = null,
    @SerialName("style") val style: String? = null,
    @SerialName("tags") val tags: List<String>? = null,
)

@Serializable
data class LookItemDto(
    @SerialName("id") val id: Int,
    @SerialName("clothe_id") val clotheId: Int,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("name") val name: String? = null,
    @SerialName("category") val category: String? = null,
)

@Serializable
data class ShareUrlDto(
    @SerialName("url") val url: String,
)
