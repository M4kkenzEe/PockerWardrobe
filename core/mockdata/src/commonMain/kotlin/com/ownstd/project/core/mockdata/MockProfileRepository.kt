package com.ownstd.project.core.mockdata

import com.ownstd.project.profile.internal.domain.model.UserModel
import com.ownstd.project.profile.internal.domain.model.UserSizesModel
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository

internal class MockProfileRepository : ProfileRepository {

    private var mockUser = UserModel(
        id = 1,
        name = "Sophia Laurent",
        email = "sophia@example.com",
        username = "sophia.style",
        gender = "FEMALE",
        avatarUrl = null,
        outfitsCount = 12,
        clothesCount = 47,
        sharedCount = 3,
    )

    private var mockSizes = UserSizesModel(
        primarySize = "S",
        height = 168f,
        weight = 58f,
        chest = 88f,
        waist = 68f,
        hips = 94f,
    )

    override suspend fun getProfile(): UserModel = mockUser

    override suspend fun updateProfile(name: String, username: String, gender: String?): UserModel {
        mockUser = mockUser.copy(name = name, username = username, gender = gender)
        return mockUser
    }

    override suspend fun getUserSizes(): UserSizesModel = mockSizes

    override suspend fun updateUserSizes(sizes: UserSizesModel): UserSizesModel {
        mockSizes = sizes
        return mockSizes
    }

    override suspend fun logout() = Unit
}
