package com.ownstd.project.pincard.internal.presentation.navigation

import kotlinx.serialization.Serializable

sealed class WardrobeNavScreens {
    @Serializable
    data object LookConstructor : WardrobeNavScreens()

    @Serializable
    data object Wardrobe : WardrobeNavScreens()

    @Serializable
    data class LookDetails(val lookId: Int? = null, val shareToken: String? = null) : WardrobeNavScreens()
}