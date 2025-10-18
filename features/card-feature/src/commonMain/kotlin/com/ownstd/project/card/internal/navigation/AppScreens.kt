package com.ownstd.project.card.internal.navigation

import kotlinx.serialization.Serializable

internal sealed class AppScreens {

    @Serializable
    data object Authorization : AppScreens()

    @Serializable
    data object Main : AppScreens()


}