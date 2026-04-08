package com.ownstd.project.profile.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.profile.internal.domain.model.UserSizes
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal class GetUserSizesUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<Outcome<UserSizes>> = flow<Outcome<UserSizes>> {
        emit(Outcome.Success(repository.getUserSizes()))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки размеров")) }
}
