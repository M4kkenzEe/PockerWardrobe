package com.ownstd.project.wardrobe.internal.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LookDto(
    val id: Int,
    val name: String,
    val url: String? = null,
)
