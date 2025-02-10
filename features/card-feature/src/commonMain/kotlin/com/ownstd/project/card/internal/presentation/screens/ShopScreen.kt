package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.runtime.Composable
import com.ownstd.project.wardrobe.api.WardrobeScreen

@Composable
internal fun ShopScreen(navigateTo: () -> Unit = {}) {
    WardrobeScreen()
}