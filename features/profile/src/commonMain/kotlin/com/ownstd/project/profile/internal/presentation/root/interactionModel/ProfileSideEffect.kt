package com.ownstd.project.profile.internal.presentation.root.interactionModel

sealed class ProfileSideEffect {
    data object NavigateToEdit : ProfileSideEffect()
    data object NavigateToSizes : ProfileSideEffect()
    data object NavigateToAuth : ProfileSideEffect()
    data class ShowError(val message: String) : ProfileSideEffect()
}
