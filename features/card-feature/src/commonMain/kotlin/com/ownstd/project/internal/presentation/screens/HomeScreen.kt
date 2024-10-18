package com.ownstd.project.internal.presentation.screens

import androidx.compose.runtime.Composable
import com.ownstd.project.WardrobeScreen


@Composable
internal fun HomeScreen(navigateTo: () -> Unit = {}) {
    WardrobeScreen()
}