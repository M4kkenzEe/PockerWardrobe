package com.ownstd.project.pincard.internal.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.data.model.LookItem

@Immutable
internal data class LookItemUiState(
    val id: Int,
    val imgUrl: String,
    val offsetX: Float = 100f,
    val offsetY: Float = 100f,
    val zIndex: Float = 0f,
    val size: Dp = 170.dp,
    val rotation: Float = 0f,
    val isSelected: Boolean = false,
)

internal fun mapper(clothe: Clothe): LookItemUiState {
    return LookItemUiState(
        id = clothe.id!!,
        imgUrl = clothe.imageUrl,
        isSelected = true,
    )
}

internal fun LookItemUiState.toData(): LookItem {
    return LookItem(
        clothe = Clothe(
            id = id,
            name = "",
            storeUrl = "",
            imageUrl = imgUrl
        ),
        size = size.value.toInt(),
        x = offsetX,
        y = offsetY,
        z = zIndex,
        rotation = rotation
    )
}