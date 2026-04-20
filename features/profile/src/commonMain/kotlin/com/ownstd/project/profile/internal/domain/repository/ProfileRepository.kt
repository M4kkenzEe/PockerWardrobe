package com.ownstd.project.profile.internal.domain.repository

import com.ownstd.project.profile.internal.domain.model.UserModel
import com.ownstd.project.profile.internal.domain.model.UserSizesModel

interface ProfileRepository {
    suspend fun getProfile(): UserModel
    suspend fun updateProfile(name: String, username: String, gender: String?): UserModel
    suspend fun getUserSizes(): UserSizesModel
    suspend fun updateUserSizes(sizes: UserSizesModel): UserSizesModel
    suspend fun logout()
}
