package com.ownstd.project.profile.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String,
    @SerialName("gender") val gender: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("outfits_count") val outfitsCount: Int = 0,
    @SerialName("clothes_count") val clothesCount: Int = 0,
    @SerialName("shared_count") val sharedCount: Int = 0,
)

@Serializable
internal data class UpdateProfileRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("gender") val gender: String? = null,
)
