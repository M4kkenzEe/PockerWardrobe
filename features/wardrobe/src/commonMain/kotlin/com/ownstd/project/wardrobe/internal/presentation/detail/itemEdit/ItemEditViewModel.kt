package com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClotheByIdUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.UpdateClotheUseCase
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel.ItemEditSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

class ItemEditViewModel(
    getClotheByIdUseCase: GetClotheByIdUseCase,
    updateClotheUseCase: UpdateClotheUseCase,
    clotheId: Int,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<ItemEditSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<ItemEditSideEffect> = _sideEffect.toSideEffect()

    val store = itemEditStore(
        getClotheByIdUseCase = getClotheByIdUseCase,
        updateClotheUseCase = updateClotheUseCase,
        clotheId = clotheId,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
