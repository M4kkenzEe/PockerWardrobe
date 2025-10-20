package com.ownstd.project.authorization.internal.domain

class LogoutUseCase(
    private val authorizationRepository: AuthorizationRepository
) {
    fun execute() {
        authorizationRepository.logout()
    }
}
