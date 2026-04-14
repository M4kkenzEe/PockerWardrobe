package com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel

import com.ownstd.project.outfitconstructor.internal.domain.model.ClotheModel
import pro.respawn.flowmvi.api.MVIIntent

sealed interface OutfitConstructorIntent : MVIIntent {
    data object LoadClothes : OutfitConstructorIntent
    data class AddItem(val clothe: ClotheModel) : OutfitConstructorIntent
    data class SelectItem(val clotheId: Int?) : OutfitConstructorIntent
    data class MoveItem(val clotheId: Int, val x: Float, val y: Float) : OutfitConstructorIntent
    data class ScaleItem(val clotheId: Int, val scale: Float) : OutfitConstructorIntent
    data class RemoveItem(val clotheId: Int) : OutfitConstructorIntent
    data object ResetCanvas : OutfitConstructorIntent
    data object ShowPicker : OutfitConstructorIntent
    data object HidePicker : OutfitConstructorIntent
    data class FilterCategory(val category: String?) : OutfitConstructorIntent
    data class SaveClicked(val captureCanvas: () -> ByteArray) : OutfitConstructorIntent
    data object BackClicked : OutfitConstructorIntent
}
