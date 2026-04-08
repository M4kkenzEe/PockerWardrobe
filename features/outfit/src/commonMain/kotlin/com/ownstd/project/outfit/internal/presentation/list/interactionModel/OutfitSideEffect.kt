package com.ownstd.project.outfit.internal.presentation.list.interactionModel

sealed class OutfitSideEffect {
    data class NavigateToDetail(val lookId: Int) : OutfitSideEffect()
    data object NavigateToConstructor : OutfitSideEffect()
    data class ShowError(val message: String) : OutfitSideEffect()
}
