package com.ownstd.project.outfitconstructor.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.outfitconstructor.internal.domain.model.ClotheModel
import com.ownstd.project.outfitconstructor.internal.domain.repository.OutfitConstructorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetClothesUseCase(private val repository: OutfitConstructorRepository) {
    operator fun invoke(): Flow<Outcome<List<ClotheModel>>> = flow {
        val clothes = repository.getClothes()
        if (clothes.isEmpty()) emit(Outcome.Empty)
        else emit(Outcome.Success(clothes))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки")) }
}
