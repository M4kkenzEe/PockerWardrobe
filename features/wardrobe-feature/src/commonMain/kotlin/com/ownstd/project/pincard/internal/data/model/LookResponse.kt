package com.ownstd.project.pincard.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LookItem(
    val clothe: Clothe,
    val size: Int,
    val x: Float,
    val y: Float,
    val z: Float,
    val rotation: Float
)

@Serializable
data class LookResponse(
    val name: String,
    val lookItems: List<LookItem>,
    val url: String
)