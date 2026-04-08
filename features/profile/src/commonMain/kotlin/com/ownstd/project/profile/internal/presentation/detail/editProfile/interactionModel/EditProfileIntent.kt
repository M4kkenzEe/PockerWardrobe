package com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel

import pro.respawn.flowmvi.api.MVIIntent

sealed interface EditProfileIntent : MVIIntent {
    data object Load : EditProfileIntent
    data class NameChanged(val value: String) : EditProfileIntent
    data class UsernameChanged(val value: String) : EditProfileIntent
    data class GenderChanged(val value: String) : EditProfileIntent
    data object SaveClicked : EditProfileIntent
    data object CancelClicked : EditProfileIntent
}
