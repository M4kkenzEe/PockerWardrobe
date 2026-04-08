package com.ownstd.project.outfit.internal.presentation.list.interactionModel

import pro.respawn.flowmvi.api.MVIIntent

sealed interface OutfitIntent : MVIIntent {
    data object LoadLooks : OutfitIntent
    data class SelectStyle(val style: String?) : OutfitIntent
    data class LookClicked(val lookId: Int) : OutfitIntent
    data class DeleteLook(val id: Int) : OutfitIntent
    data class ShareLook(val id: Int) : OutfitIntent
    data class FavoriteLook(val id: Int) : OutfitIntent
    data object FabClicked : OutfitIntent
}
