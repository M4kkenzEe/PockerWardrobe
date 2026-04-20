package com.ownstd.project.profile.internal.domain.usecase

import com.ownstd.project.profile.internal.domain.model.UserSizesModel
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository

internal class UpdateUserSizesUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(sizes: UserSizesModel): Result<UserSizesModel> =
        runCatching { repository.updateUserSizes(sizes) }
}
