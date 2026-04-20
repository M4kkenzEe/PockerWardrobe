package com.ownstd.project.profile.internal.domain.model

data class UserModel(
    val id: Int,
    val name: String,
    val email: String,
    val username: String,
    val gender: String? = null,
    val avatarUrl: String? = null,
    val outfitsCount: Int = 0,
    val clothesCount: Int = 0,
    val sharedCount: Int = 0,
)
