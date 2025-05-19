package com.ownstd.project.tiktok_feed.model

import org.jetbrains.compose.resources.DrawableResource

data class Feed(
    val id: Int,
    val user: String,
    val clothes: List<Clothe>
)

data class Clothe(
    val imageId: Int,
    val image: DrawableResource,
    val x: Float,
    val y: Float,
    val size: Int,
    val rotationAngle: Float
)
