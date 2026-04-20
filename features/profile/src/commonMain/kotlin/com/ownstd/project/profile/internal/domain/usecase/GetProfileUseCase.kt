package com.ownstd.project.profile.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.profile.internal.domain.model.UserModel
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal class GetProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<Outcome<UserModel>> = flow<Outcome<UserModel>> {
        emit(Outcome.Success(repository.getProfile()))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки профиля")) }
}
