package com.ownstd.project.outfit.internal.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.outfit.internal.domain.usecase.DeleteLookUseCase
import com.ownstd.project.outfit.internal.domain.usecase.GetLooksUseCase
import com.ownstd.project.outfit.internal.domain.usecase.ShareLookUseCase
import com.ownstd.project.outfit.internal.presentation.list.interactionModel.OutfitSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

class OutfitViewModel(
    getLooksUseCase: GetLooksUseCase,
    deleteLookUseCase: DeleteLookUseCase,
    shareLookUseCase: ShareLookUseCase,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<OutfitSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<OutfitSideEffect> = _sideEffect.toSideEffect()

    val store = outfitStore(
        getLooksUseCase = getLooksUseCase,
        deleteLookUseCase = deleteLookUseCase,
        shareLookUseCase = shareLookUseCase,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
