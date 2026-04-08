package com.ownstd.project.outfit.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.outfit.internal.domain.model.Look
import com.ownstd.project.outfit.internal.domain.repository.OutfitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetLooksUseCase(private val repository: OutfitRepository) {
    operator fun invoke(): Flow<Outcome<List<Look>>> = flow {
        val looks = repository.getLooks()
        if (looks.isEmpty()) emit(Outcome.Empty)
        else emit(Outcome.Success(looks))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки")) }
}
