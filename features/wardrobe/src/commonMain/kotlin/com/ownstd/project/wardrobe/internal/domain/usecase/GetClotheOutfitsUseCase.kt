package com.ownstd.project.wardrobe.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.wardrobe.internal.domain.model.Look
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetClotheOutfitsUseCase(private val repository: WardrobeRepository) {
    operator fun invoke(clotheId: Int): Flow<Outcome<List<Look>>> = flow {
        val looks = repository.getClotheOutfits(clotheId)
        if (looks.isEmpty()) emit(Outcome.Empty)
        else emit(Outcome.Success(looks))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки образов")) }
}
