package com.ownstd.project.profile.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserDto(
    val name: String,
    val email: String,
    val gender: String? = null,
)

@Serializable
internal data class UpdateProfileRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("gender") val gender: String? = null,
)
