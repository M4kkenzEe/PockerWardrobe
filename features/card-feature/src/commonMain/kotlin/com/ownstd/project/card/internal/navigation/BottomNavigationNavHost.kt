package com.ownstd.project.card.internal.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ownstd.project.card.internal.presentation.screens.HomeScreen
import com.ownstd.project.card.internal.presentation.screens.ProfileScreen
import com.ownstd.project.card.internal.presentation.screens.SettingsScreen
import com.ownstd.project.card.internal.presentation.screens.Wardrobe
import com.ownstd.project.pincard.internal.presentation.navigation.wardrobeNavGraph

@Composable
internal fun BottomNavigationNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavigationScreens.Home(),
        modifier = modifier.fillMaxSize()
    ) {
        composable<BottomNavigationScreens.Home> {
            HomeScreen()
        }

        composable<BottomNavigationScreens.Shop> {
            Wardrobe(navController)
        }

        composable<BottomNavigationScreens.Profile> {
            ProfileScreen()
        }

        composable<BottomNavigationScreens.Outfits> {
            SettingsScreen()
        }

        wardrobeNavGraph(navController)
    }
}

