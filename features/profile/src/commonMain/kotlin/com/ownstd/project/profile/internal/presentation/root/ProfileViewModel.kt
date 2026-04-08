package com.ownstd.project.profile.internal.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.profile.internal.domain.usecase.GetProfileUseCase
import com.ownstd.project.profile.internal.domain.usecase.ProfileLogoutUseCase
import com.ownstd.project.profile.internal.presentation.root.interactionModel.ProfileSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

internal class ProfileViewModel(
    getProfileUseCase: GetProfileUseCase,
    logoutUseCase: ProfileLogoutUseCase,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<ProfileSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<ProfileSideEffect> = _sideEffect.toSideEffect()

    val store = profileStore(
        getProfileUseCase = getProfileUseCase,
        logoutUseCase = logoutUseCase,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
