package com.ownstd.project.profile.presentation

import kotlinx.serialization.Serializable

sealed class ProfileNavScreens {
    @Serializable
    data object Profile : ProfileNavScreens()

    @Serializable
    data object Settings : ProfileNavScreens()
}
