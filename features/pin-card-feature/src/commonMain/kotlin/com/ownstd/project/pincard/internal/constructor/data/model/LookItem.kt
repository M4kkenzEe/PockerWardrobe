package com.ownstd.project.pincard.internal.constructor.data.model

import com.ownstd.project.pincard.internal.data.model.Clothe
import kotlinx.serialization.Serializable

@Serializable
data class LookItem(
    val clothe: Clothe,
    val size: Int,
    val x: Float,
    val y: Float,
    val rotation: Float
)