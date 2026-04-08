package com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.wardrobe.internal.domain.usecase.DeleteClotheUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClotheByIdUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClotheOutfitsUseCase
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel.ItemDetailSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

class ItemDetailViewModel(
    getClotheByIdUseCase: GetClotheByIdUseCase,
    getClotheOutfitsUseCase: GetClotheOutfitsUseCase,
    deleteClotheUseCase: DeleteClotheUseCase,
    clotheId: Int,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<ItemDetailSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<ItemDetailSideEffect> = _sideEffect.toSideEffect()

    val store = itemDetailStore(
        getClotheByIdUseCase = getClotheByIdUseCase,
        getClotheOutfitsUseCase = getClotheOutfitsUseCase,
        deleteClotheUseCase = deleteClotheUseCase,
        clotheId = clotheId,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
