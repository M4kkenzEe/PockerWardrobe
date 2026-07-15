package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ownstd.project.card.internal.deeplink.DeepLink
import com.ownstd.project.card.internal.deeplink.getDeepLinkManager
import com.ownstd.project.card.internal.navigation.AppScreens
import com.ownstd.project.card.internal.navigation.BottomNavigationNavHost
import com.ownstd.project.card.internal.navigation.BottomNavigationScreens
import com.ownstd.project.designsystem.components.ClothisNavIsland
import com.ownstd.project.designsystem.components.NavTab
import com.ownstd.project.designsystem.theme.ClothisTheme

@Composable
internal fun MainScreen(parentNavController: NavHostController) {
    val bottomNavController = rememberNavController()
    val deepLinkManager = getDeepLinkManager()
    val currentDeepLink by deepLinkManager.deepLinkFlow.collectAsState()
    val initialDeepLink = remember { currentDeepLink }
    var selectedTab by remember { mutableStateOf(NavTab.Wardrobe) }
    val colors = ClothisTheme.colors
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val showNavIsland = listOf("ClothingDetail", "LookDetails", "LookConstructor", "TinderOutfit")
        .none { currentRoute.contains(it) }

    LaunchedEffect(currentDeepLink) {
        if (currentDeepLink != null) {
            deepLinkManager.clearDeepLink()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        BottomNavigationNavHost(
            navController = bottomNavController,
            deepLink = initialDeepLink,
            onLogout = {
                parentNavController.navigate(AppScreens.Authorization) {
                    popUpTo(AppScreens.Main) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Floating glass nav island — hidden on detail screens
        if (showNavIsland) {
            ClothisNavIsland(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    val destination = when (tab) {
                        NavTab.Wardrobe -> BottomNavigationScreens.Shop()
                        NavTab.Profile -> BottomNavigationScreens.Profile()
                    }
                    bottomNavController.navigate(destination) {
                        popUpTo(bottomNavController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
