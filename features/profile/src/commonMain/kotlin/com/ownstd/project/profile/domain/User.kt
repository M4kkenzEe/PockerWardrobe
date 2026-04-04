package com.ownstd.project.profile.domain

import androidx.compose.runtime.Stable
import com.ownstd.project.profile.presentation.Gender

@Stable
data class User(val name: String, val email: String, val gender: Gender)