package com.ownstd.project.profile.internal.data.mapper

import com.ownstd.project.profile.internal.data.dto.UpdateProfileRequest
import com.ownstd.project.profile.internal.data.dto.UserDto
import com.ownstd.project.profile.internal.domain.model.User

internal fun UserDto.toUser(): User = User(
    id = id,
    name = name,
    email = email,
    username = username,
    gender = gender,
    avatarUrl = avatarUrl,
    outfitsCount = outfitsCount,
    clothesCount = clothesCount,
    sharedCount = sharedCount,
)

internal fun User.toUpdateRequest(): UpdateProfileRequest = UpdateProfileRequest(
    name = name,
    username = username,
    gender = gender,
)
