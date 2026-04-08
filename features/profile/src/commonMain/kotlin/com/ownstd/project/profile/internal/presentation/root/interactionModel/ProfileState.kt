package com.ownstd.project.profile.internal.presentation.root.interactionModel

import com.ownstd.project.profile.internal.domain.model.User
import pro.respawn.flowmvi.api.MVIState

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean = false,
    val showLogoutDialog: Boolean = false,
) : MVIState
