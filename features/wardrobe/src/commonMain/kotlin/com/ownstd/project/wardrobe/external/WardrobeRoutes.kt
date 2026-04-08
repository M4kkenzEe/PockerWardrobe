package com.ownstd.project.wardrobe.external

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class WardrobeRoutes : NavKey {
    @Serializable data object Main : WardrobeRoutes()
    @Serializable data class ItemDetail(val clotheId: Int) : WardrobeRoutes()
    @Serializable data class ItemEdit(val clotheId: Int) : WardrobeRoutes()
}
