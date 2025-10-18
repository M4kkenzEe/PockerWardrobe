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
data class Look(
    val id: Int? = null,
    val name: String,
    val url: String,
    val lookItems: List<LookItem>? = null
)

@Serializable
data class DraftLook(
    val name: String,
    val lookItems: List<LookItem>
)


@Serializable
data class SharedLookResponse(
    val look: Look,
    val shareToken: String
)