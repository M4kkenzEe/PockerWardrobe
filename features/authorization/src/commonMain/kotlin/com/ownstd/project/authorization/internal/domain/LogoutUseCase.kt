package com.ownstd.project.authorization.internal.domain

import com.ownstd.project.network.api.NetworkRepository

class LogoutUseCase(
    private val authorizationRepository: AuthorizationRepository,
    private val networkRepository: NetworkRepository
) {
    suspend fun execute() {
        authorizationRepository.logout()
        networkRepository.clearAuthCache()
    }
}
