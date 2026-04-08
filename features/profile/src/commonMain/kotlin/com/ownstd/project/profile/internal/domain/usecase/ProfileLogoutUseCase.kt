package com.ownstd.project.profile.internal.domain.usecase

import com.ownstd.project.profile.internal.domain.repository.ProfileRepository

internal class ProfileLogoutUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Result<Unit> =
        runCatching { repository.logout() }
}
