package com.ownstd.project.profile.internal.domain.usecase

import com.ownstd.project.profile.internal.domain.model.User
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository

internal class UpdateProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(
        name: String,
        username: String,
        gender: String?,
    ): Result<User> = runCatching {
        repository.updateProfile(name, username, gender)
    }
}
