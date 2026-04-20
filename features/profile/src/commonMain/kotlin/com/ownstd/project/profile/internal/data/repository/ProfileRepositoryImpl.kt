package com.ownstd.project.profile.internal.data.repository

import com.ownstd.project.profile.internal.data.api.ProfileApi
import com.ownstd.project.profile.internal.data.dto.UpdateProfileRequest
import com.ownstd.project.profile.internal.data.mapper.toDto
import com.ownstd.project.profile.internal.data.mapper.toUser
import com.ownstd.project.profile.internal.data.mapper.toUserSizes
import com.ownstd.project.profile.internal.domain.model.UserModel
import com.ownstd.project.profile.internal.domain.model.UserSizesModel
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository
import com.ownstd.project.storage.TokenStorage

internal class ProfileRepositoryImpl(
    private val api: ProfileApi,
    private val tokenStorage: TokenStorage,
) : ProfileRepository {

    override suspend fun getProfile(): UserModel =
        api.getProfile().toUser()

    override suspend fun updateProfile(
        name: String,
        username: String,
        gender: String?,
    ): UserModel = api.updateProfile(
        UpdateProfileRequest(name = name, username = username, gender = gender)
    ).toUser()

    override suspend fun getUserSizes(): UserSizesModel =
        api.getUserSizes().toUserSizes()

    override suspend fun updateUserSizes(sizes: UserSizesModel): UserSizesModel =
        api.updateUserSizes(sizes.toDto()).toUserSizes()

    override suspend fun logout() {
        tokenStorage.clearSession()
    }
}
