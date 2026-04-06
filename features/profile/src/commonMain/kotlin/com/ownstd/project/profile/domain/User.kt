package com.ownstd.project.profile.domain

import androidx.compose.runtime.Stable
import com.ownstd.project.profile.presentation.Gender

@Stable
data class User(
    val name: String,
    val email: String,
    val gender: Gender,
    val username: String = "",
    val avatarUrl: String = "",
    val outfitsCount: Int = 0,
    val clothesCount: Int = 0,
    val sharedCount: Int = 0,
)