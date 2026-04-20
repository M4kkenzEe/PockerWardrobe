package com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel

import com.ownstd.project.profile.internal.domain.model.UserModel
import pro.respawn.flowmvi.api.MVIState

data class EditProfileState(
    val user: UserModel? = null,
    val name: String = "",
    val username: String = "",
    val gender: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) : MVIState
