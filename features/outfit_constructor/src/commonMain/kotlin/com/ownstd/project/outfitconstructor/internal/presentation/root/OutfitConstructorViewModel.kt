package com.ownstd.project.outfitconstructor.internal.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.outfitconstructor.internal.domain.usecase.AddLookUseCase
import com.ownstd.project.outfitconstructor.internal.domain.usecase.GetClothesUseCase
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.OutfitConstructorSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

class OutfitConstructorViewModel(
    getClothesUseCase: GetClothesUseCase,
    addLookUseCase: AddLookUseCase,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<OutfitConstructorSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<OutfitConstructorSideEffect> = _sideEffect.toSideEffect()

    val store = outfitConstructorStore(
        getClothesUseCase = getClothesUseCase,
        addLookUseCase = addLookUseCase,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
