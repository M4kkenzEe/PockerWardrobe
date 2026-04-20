package com.ownstd.project.profile.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.profile.internal.domain.model.UserSizesModel
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal class GetUserSizesUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<Outcome<UserSizesModel>> = flow<Outcome<UserSizesModel>> {
        emit(Outcome.Success(repository.getUserSizes()))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки размеров")) }
}
