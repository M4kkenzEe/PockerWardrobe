package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ownstd.project.pincard.internal.presentation.viewmodel.AddLookMode
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun DraftTopBarPreview(isAddingLook: Boolean = false, lookAdded: Boolean = false) {
    Box(Modifier.fillMaxWidth().background(Color.DarkGray)) {
        LookDetailsTopBar(
            lookName = "Образ с рубашкой",
            onBackClick = {},
            onAddToWardrobe = {},
            addLookMode = AddLookMode.Draft,
            isAddingLook = isAddingLook,
            lookAdded = lookAdded,
        )
    }
}

@Preview
@Composable
fun LookDraftNotAddedPreview() = DraftTopBarPreview()

@Preview
@Composable
fun LookDraftAddingPreview() = DraftTopBarPreview(isAddingLook = true)

@Preview
@Composable
fun LookDraftAddedPreview() = DraftTopBarPreview(lookAdded = true)
