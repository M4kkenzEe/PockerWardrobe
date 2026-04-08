package com.ownstd.project.wardrobe.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.wardrobe.internal.domain.model.Clothe
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetClothesUseCase(private val repository: WardrobeRepository) {
    operator fun invoke(): Flow<Outcome<List<Clothe>>> = flow<Outcome<List<Clothe>>> {
        val clothes = repository.getClothes()
        if (clothes.isEmpty()) emit(Outcome.Empty)
        else emit(Outcome.Success(clothes))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки")) }
}
