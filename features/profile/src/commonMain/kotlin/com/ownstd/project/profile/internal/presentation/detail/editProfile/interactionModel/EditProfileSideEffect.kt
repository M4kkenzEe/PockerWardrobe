package com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel

sealed class EditProfileSideEffect {
    data object NavigateBack : EditProfileSideEffect()
    data class ShowError(val message: String) : EditProfileSideEffect()
}
