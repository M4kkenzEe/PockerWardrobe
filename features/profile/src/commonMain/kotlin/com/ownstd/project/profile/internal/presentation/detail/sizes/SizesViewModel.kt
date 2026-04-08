package com.ownstd.project.profile.internal.presentation.detail.sizes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.profile.internal.domain.usecase.GetUserSizesUseCase
import com.ownstd.project.profile.internal.domain.usecase.UpdateUserSizesUseCase
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizesSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

internal class SizesViewModel(
    getUserSizesUseCase: GetUserSizesUseCase,
    updateUserSizesUseCase: UpdateUserSizesUseCase,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<SizesSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<SizesSideEffect> = _sideEffect.toSideEffect()

    val store = sizesStore(
        getUserSizesUseCase = getUserSizesUseCase,
        updateUserSizesUseCase = updateUserSizesUseCase,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
