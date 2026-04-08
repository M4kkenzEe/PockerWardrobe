package com.ownstd.project.profile.internal.presentation.root.interactionModel

import pro.respawn.flowmvi.api.MVIIntent

sealed interface ProfileIntent : MVIIntent {
    data object Load : ProfileIntent
    data object EditProfileClicked : ProfileIntent
    data object SizesClicked : ProfileIntent
    data object ToggleTheme : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data object LogoutConfirmed : ProfileIntent
    data object LogoutDismissed : ProfileIntent
}
