package com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel

sealed class OutfitConstructorSideEffect {
    data object NavigateBack : OutfitConstructorSideEffect()
    data class ShowError(val message: String) : OutfitConstructorSideEffect()
}
