package com.ownstd.project.outfit.internal.presentation.detail.interactionModel

import pro.respawn.flowmvi.api.MVIIntent

sealed interface OutfitDetailIntent : MVIIntent {
    data class Load(val lookId: Int) : OutfitDetailIntent
    data object MenuClicked : OutfitDetailIntent
    data object EditClicked : OutfitDetailIntent
    data object DeleteClicked : OutfitDetailIntent
    data object DeleteConfirmed : OutfitDetailIntent
    data object DeleteCancelled : OutfitDetailIntent
    data object ShareClicked : OutfitDetailIntent
    data object FavoriteClicked : OutfitDetailIntent
    data class ItemClicked(val clotheId: Int) : OutfitDetailIntent
}
