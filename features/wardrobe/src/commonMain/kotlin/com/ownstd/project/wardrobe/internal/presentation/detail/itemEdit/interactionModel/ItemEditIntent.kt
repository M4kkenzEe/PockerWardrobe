package com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel

import pro.respawn.flowmvi.api.MVIIntent

sealed interface ItemEditIntent : MVIIntent {
    data class Load(val clotheId: Int) : ItemEditIntent
    data class NameChanged(val value: String) : ItemEditIntent
    data class CategoryChanged(val value: String) : ItemEditIntent
    data class SizeChanged(val value: String) : ItemEditIntent
    data class MaterialChanged(val value: String) : ItemEditIntent
    data class FitChanged(val value: String) : ItemEditIntent
    data class StylesChanged(val value: String) : ItemEditIntent
    data class SeasonChanged(val value: String) : ItemEditIntent
    data class BrandChanged(val value: String) : ItemEditIntent
    data class MarketplaceLinkChanged(val index: Int, val url: String) : ItemEditIntent
    data class MarketplaceLinkRemoved(val index: Int) : ItemEditIntent
    data object AddMarketplaceLink : ItemEditIntent
    data object SaveClicked : ItemEditIntent
    data object CancelClicked : ItemEditIntent
}
