package com.ownstd.project.outfit.internal.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.outfit.internal.domain.usecase.DeleteLookUseCase
import com.ownstd.project.outfit.internal.domain.usecase.GetLookByIdUseCase
import com.ownstd.project.outfit.internal.domain.usecase.ShareLookUseCase
import com.ownstd.project.outfit.internal.presentation.detail.interactionModel.OutfitDetailSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

class OutfitDetailViewModel(
    getLookByIdUseCase: GetLookByIdUseCase,
    deleteLookUseCase: DeleteLookUseCase,
    shareLookUseCase: ShareLookUseCase,
    lookId: Int,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<OutfitDetailSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<OutfitDetailSideEffect> = _sideEffect.toSideEffect()

    val store = outfitDetailStore(
        getLookByIdUseCase = getLookByIdUseCase,
        deleteLookUseCase = deleteLookUseCase,
        shareLookUseCase = shareLookUseCase,
        lookId = lookId,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
