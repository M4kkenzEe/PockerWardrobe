package com.ownstd.project.profile.internal.data.mapper

import com.ownstd.project.profile.internal.data.dto.UpdateProfileRequest
import com.ownstd.project.profile.internal.data.dto.UserDto
import com.ownstd.project.profile.internal.domain.model.UserModel

internal fun UserDto.toUser(): UserModel = UserModel(
    id = 0,
    name = name,
    email = email,
    username = name,
    gender = gender,
    avatarUrl = null,
    outfitsCount = 0,
    clothesCount = 0,
    sharedCount = 0,
)

internal fun UserModel.toUpdateRequest(): UpdateProfileRequest = UpdateProfileRequest(
    name = name,
    username = username,
    gender = gender,
)
