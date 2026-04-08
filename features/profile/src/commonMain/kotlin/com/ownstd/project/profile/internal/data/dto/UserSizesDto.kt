package com.ownstd.project.profile.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserSizesDto(
    @SerialName("primary_size") val primarySize: String? = null,
    @SerialName("height") val height: Float? = null,
    @SerialName("weight") val weight: Float? = null,
    @SerialName("chest") val chest: Float? = null,
    @SerialName("waist") val waist: Float? = null,
    @SerialName("hips") val hips: Float? = null,
)
