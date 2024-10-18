package com.ownstd.project.internal.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ownstd.project.internal.presentation.screens.HomeScreen
import com.ownstd.project.internal.presentation.screens.ProfileScreen
import com.ownstd.project.internal.presentation.screens.SettingsScreen
import com.ownstd.project.internal.presentation.screens.ShopScreen

@Composable
internal fun BottomNavigationNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavigationItems.HOME.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(BottomNavigationItems.HOME.route) {
            HomeScreen()
        }

        composable(BottomNavigationItems.SHOP.route) {
            ShopScreen()
        }

        composable(BottomNavigationItems.PROFILE.route) {
            ProfileScreen()
        }

        composable(BottomNavigationItems.SETTINGS.route) {
            SettingsScreen()
        }
    }
}

