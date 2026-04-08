package com.ownstd.project.profile.internal.presentation.detail.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.core.compose.foundation.SideEffect
import com.ownstd.project.core.compose.foundation.toSideEffect
import com.ownstd.project.profile.internal.domain.usecase.GetProfileUseCase
import com.ownstd.project.profile.internal.domain.usecase.UpdateProfileUseCase
import com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel.EditProfileSideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

private const val SIDE_EFFECT_BUFFER_CAPACITY = 1

internal class EditProfileViewModel(
    getProfileUseCase: GetProfileUseCase,
    updateProfileUseCase: UpdateProfileUseCase,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<EditProfileSideEffect>(
        extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY,
    )
    val sideEffect: SideEffect<EditProfileSideEffect> = _sideEffect.toSideEffect()

    val store = editProfileStore(
        getProfileUseCase = getProfileUseCase,
        updateProfileUseCase = updateProfileUseCase,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { it.start(viewModelScope) }
}
