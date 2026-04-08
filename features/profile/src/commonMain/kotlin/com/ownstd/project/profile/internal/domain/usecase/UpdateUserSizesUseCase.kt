package com.ownstd.project.profile.internal.domain.usecase

import com.ownstd.project.profile.internal.domain.model.UserSizes
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository

internal class UpdateUserSizesUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(sizes: UserSizes): Result<UserSizes> =
        runCatching { repository.updateUserSizes(sizes) }
}
