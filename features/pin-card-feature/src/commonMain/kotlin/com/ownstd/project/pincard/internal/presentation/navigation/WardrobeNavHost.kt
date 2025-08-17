package com.ownstd.project.pincard.internal.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ownstd.project.pincard.internal.presentation.compose.LookConstructor
import com.ownstd.project.pincard.internal.presentation.compose.WardrobeMainScreen

@Composable
fun WardrobeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = WardrobeNavScreens.Wardrobe,
        modifier = modifier.fillMaxSize()
    ) {
        composable<WardrobeNavScreens.Wardrobe> {
            WardrobeMainScreen {
                navController.navigate(WardrobeNavScreens.LookConstructor) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }

        composable<WardrobeNavScreens.LookConstructor> {
            LookConstructor()
        }
    }
}