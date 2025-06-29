package com.ownstd.project.profile.domain

import com.ownstd.project.profile.data.ProfileResponse

internal interface ProfileRepository {
    suspend fun getProfile(): ProfileResponse
}