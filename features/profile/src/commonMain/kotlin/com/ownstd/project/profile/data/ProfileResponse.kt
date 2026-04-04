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
) {
    companion object Companion
}

fun ProfileResponse.toUser(): User {
    return User(
        name = name,
        email = email,
        gender = Gender.valueOf(gender),
    )
}

fun ProfileResponse.Companion.emptyBody(): ProfileResponse {
    return ProfileResponse(
        name = "",
        email = "",
        gender = "OTHER",
    )
}