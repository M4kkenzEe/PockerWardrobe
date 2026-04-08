package com.ownstd.project.wardrobe.internal.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.wardrobe.internal.domain.usecase.DeleteClotheUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClothesUseCase
import com.ownstd.project.wardrobe.internal.presentation.list.interactionModel.WardrobeSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

class WardrobeViewModel(
    getClothesUseCase: GetClothesUseCase,
    deleteClotheUseCase: DeleteClotheUseCase,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<WardrobeSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<WardrobeSideEffect> = _sideEffect.toSideEffect()

    val store = wardrobeStore(
        getClothesUseCase = getClothesUseCase,
        deleteClotheUseCase = deleteClotheUseCase,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
