package com.ownstd.project.profile.data

import com.ownstd.project.profile.presentation.Gender
import com.ownstd.project.profile.domain.User
import kotlinx.serialization.Serializable
import kotlin.String

@Serializable
data class ProfileResponse(
    val name: String,
    val email: String,
    val gender: String,
    val clothes_count: Int = 0,
    val looks_count: Int = 0,
) {
    companion object Companion
}

fun ProfileResponse.toUser(): User {
    return User(
        name = name,
        email = email,
        gender = Gender.valueOf(gender),
        clothesCount = clothes_count,
        looksCount = looks_count,
    )
}

fun ProfileResponse.Companion.emptyBody(): ProfileResponse {
    return ProfileResponse(
        name = "",
        email = "",
        gender = "OTHER",
        clothes_count = 0,
        looks_count = 0,
    )
}