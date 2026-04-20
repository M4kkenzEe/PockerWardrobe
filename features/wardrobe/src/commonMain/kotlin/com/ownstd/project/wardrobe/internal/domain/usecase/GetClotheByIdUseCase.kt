package com.ownstd.project.wardrobe.internal.domain.usecase

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetailModel
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetClotheByIdUseCase(private val repository: WardrobeRepository) {
    operator fun invoke(clotheId: Int): Flow<Outcome<ClotheDetailModel>> = flow<Outcome<ClotheDetailModel>> {
        emit(Outcome.Success(repository.getClotheById(clotheId)))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки")) }
}
