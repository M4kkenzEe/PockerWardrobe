package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.ownstd.project.pincard.internal.presentation.WardrobeMainScreen


@Composable
internal fun Wardrobe(navController: NavHostController) {
    WardrobeMainScreen { destination -> navController.navigate(destination) }
}