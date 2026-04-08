package com.ownstd.project.outfit.external

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class OutfitRoutes : NavKey {
    @Serializable
    data object OutfitMain : OutfitRoutes()

    @Serializable
    data class OutfitDetail(val lookId: Int) : OutfitRoutes()
}
