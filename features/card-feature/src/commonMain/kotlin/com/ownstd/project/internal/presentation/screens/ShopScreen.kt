package com.ownstd.project.internal.presentation.screens

import androidx.compose.runtime.Composable
import com.ownstd.project.api.WardrobeScreen

@Composable
internal fun ShopScreen(navigateTo: () -> Unit = {}) {
    WardrobeScreen()
}