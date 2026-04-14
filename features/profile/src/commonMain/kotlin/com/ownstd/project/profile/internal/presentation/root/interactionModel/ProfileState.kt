package com.ownstd.project.profile.internal.presentation.root.interactionModel

import com.ownstd.project.profile.internal.domain.model.UserModel
import pro.respawn.flowmvi.api.MVIState

data class ProfileState(
    val user: UserModel? = null,
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean = false,
    val showLogoutDialog: Boolean = false,
) : MVIState
