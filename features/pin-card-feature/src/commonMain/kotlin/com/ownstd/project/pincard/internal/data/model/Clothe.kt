package com.ownstd.project.pincard.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Clothe(
    val name: String,
    val storeUrl: String,
    val imageUrl: String,
)