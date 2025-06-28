package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.runtime.Composable
import com.ownstd.project.api.RecommendationsPageScreen

@Composable
internal fun HomeScreen(navigateTo: () -> Unit = {}) {
    RecommendationsPageScreen()
}