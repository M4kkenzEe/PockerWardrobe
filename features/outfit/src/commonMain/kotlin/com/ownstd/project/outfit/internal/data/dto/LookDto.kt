package com.ownstd.project.outfit.internal.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LookDto(
    val id: Int? = null,
    val name: String,
    val url: String,
    val lookItems: List<LookItemDto>? = null,
)

@Serializable
data class ClotheSummaryDto(
    val id: Int? = null,
    val imageUrl: String? = null,
    val name: String,
    val category: String? = null,
)

@Serializable
data class LookItemDto(
    val clothe: ClotheSummaryDto,
    val size: Int,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val rotation: Float = 0f,
)

@Serializable
data class ShareUrlDto(
    val shareToken: String,
    val shareUrl: String,
)
