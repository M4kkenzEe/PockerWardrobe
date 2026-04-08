package com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel

import pro.respawn.flowmvi.api.MVIIntent

sealed interface ItemDetailIntent : MVIIntent {
    data class Load(val clotheId: Int) : ItemDetailIntent
    data object EditClicked : ItemDetailIntent
    data object DeleteClicked : ItemDetailIntent
    data object DeleteConfirmed : ItemDetailIntent
    data object DeleteCancelled : ItemDetailIntent
    data class LookClicked(val lookId: Int) : ItemDetailIntent
    data class MarketplaceLinkClicked(val url: String) : ItemDetailIntent
}
