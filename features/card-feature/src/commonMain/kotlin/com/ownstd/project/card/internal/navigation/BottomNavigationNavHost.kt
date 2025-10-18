package com.ownstd.project.card.internal.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ownstd.project.card.internal.deeplink.DeepLink
import com.ownstd.project.card.internal.presentation.screens.HomeScreen
import com.ownstd.project.card.internal.presentation.screens.ProfileScreen
import com.ownstd.project.pincard.internal.presentation.navigation.WardrobeNavScreens
import com.ownstd.project.pincard.internal.presentation.navigation.wardrobeNavGraph

@Composable
internal fun BottomNavigationNavHost(
    navController: NavHostController,
    deepLink: DeepLink? = null,
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
            LaunchedEffect(Unit) {
                navController.navigate(WardrobeNavScreens.Wardrobe) {
                    popUpTo(BottomNavigationScreens.Shop()) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable<BottomNavigationScreens.Profile> {
            ProfileScreen()
        }

        wardrobeNavGraph(navController)
    }

    LaunchedEffect(deepLink) {
        if (deepLink is DeepLink.Look) {
            val backStackSize = navController.currentBackStack.value.size
            if (backStackSize <= 2) {
                navController.navigate(
                    WardrobeNavScreens.LookDetails(shareToken = deepLink.shareToken)
                )
            }
        }
    }
}

