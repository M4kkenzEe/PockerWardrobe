package com.ownstd.project.pincard.internal.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ownstd.project.pincard.internal.presentation.WardrobeMainScreen
import com.ownstd.project.pincard.internal.presentation.compose.LookConstructor

fun NavGraphBuilder.wardrobeNavGraph(navController: NavHostController) {
    composable<WardrobeNavScreens.Wardrobe> {
        WardrobeMainScreen {
            navController.navigate(WardrobeNavScreens.LookConstructor)
        }
    }
    composable<WardrobeNavScreens.LookConstructor> {
        LookConstructor { navController.popBackStack() }
    }
}
