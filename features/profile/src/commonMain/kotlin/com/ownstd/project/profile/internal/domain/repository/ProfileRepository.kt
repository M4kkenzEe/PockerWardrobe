package com.ownstd.project.profile.internal.domain.repository

import com.ownstd.project.profile.internal.domain.model.User
import com.ownstd.project.profile.internal.domain.model.UserSizes

internal interface ProfileRepository {
    suspend fun getProfile(): User
    suspend fun updateProfile(name: String, username: String, gender: String?): User
    suspend fun getUserSizes(): UserSizes
    suspend fun updateUserSizes(sizes: UserSizes): UserSizes
    suspend fun logout()
}
