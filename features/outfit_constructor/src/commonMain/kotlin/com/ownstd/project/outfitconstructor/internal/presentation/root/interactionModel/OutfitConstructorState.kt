package com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel

import com.ownstd.project.outfitconstructor.internal.domain.model.Clothe
import pro.respawn.flowmvi.api.MVIState

data class CanvasItem(
    val clothe: Clothe,
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val isSelected: Boolean = false,
)

data class OutfitConstructorState(
    val canvasItems: List<CanvasItem> = emptyList(),
    val availableClothes: List<Clothe> = emptyList(),
    val activeCategory: String? = null,
    val isPickerVisible: Boolean = false,
    val isLoadingClothes: Boolean = false,
    val isSaving: Boolean = false,
) : MVIState {
    val selectedItem: CanvasItem? get() = canvasItems.firstOrNull { it.isSelected }
    val canSave: Boolean get() = canvasItems.isNotEmpty()

    val filteredClothes: List<Clothe>
        get() = if (activeCategory != null) {
            availableClothes.filter { it.category == activeCategory }
        } else {
            availableClothes
        }

    val addedClotheIds: Set<Int?>
        get() = canvasItems.map { it.clothe.id }.toSet()
}
