package com.ownstd.project.pincard.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Look(
    val name: String,
    val lookItems: List<LookItem>
)