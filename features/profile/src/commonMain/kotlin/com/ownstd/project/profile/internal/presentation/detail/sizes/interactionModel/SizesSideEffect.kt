package com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel

sealed class SizesSideEffect {
    data class ShowError(val message: String) : SizesSideEffect()
}
