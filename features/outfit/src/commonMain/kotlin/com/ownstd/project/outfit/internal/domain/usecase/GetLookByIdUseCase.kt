package com.ownstd.project.outfit.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.outfit.internal.domain.model.Look
import com.ownstd.project.outfit.internal.domain.repository.OutfitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetLookByIdUseCase(private val repository: OutfitRepository) {
    operator fun invoke(lookId: Int): Flow<Outcome<Look>> = flow<Outcome<Look>> {
        emit(Outcome.Success(repository.getLookById(lookId)))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки")) }
}
