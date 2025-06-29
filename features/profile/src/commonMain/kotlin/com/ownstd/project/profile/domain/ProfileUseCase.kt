package com.ownstd.project.profile.domain

import com.ownstd.project.profile.data.toUser
import com.ownstd.project.profile.domain.User

internal class ProfileUseCase(private val repository: ProfileRepository) {
    suspend fun getProfile(): User {
        return repository.getProfile().toUser()
    }
}