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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ownstd.project.card.internal.deeplink.DeepLink
import com.ownstd.project.card.internal.deeplink.getDeepLinkManager
import com.ownstd.project.card.internal.navigation.AppScreens
import com.ownstd.project.card.internal.navigation.BottomNavigationNavHost
import com.ownstd.project.card.internal.navigation.BottomNavigationScreens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.ownstd.project.designsystem.components.ClothisNavIsland
import com.ownstd.project.designsystem.components.GlassIconButton
import com.ownstd.project.designsystem.components.NavTab
import com.ownstd.project.designsystem.theme.ClothisTheme
import kotlinprojecttesting.design_system.generated.resources.Res
import kotlinprojecttesting.design_system.generated.resources.ic_search
import kotlinprojecttesting.design_system.generated.resources.ic_tune
import org.jetbrains.compose.resources.painterResource

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

        // Floating bottom controls — filter | nav island | search
        if (showNavIsland) {
            val dimens = ClothisTheme.dimens
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = dimens.navIslandBottomGap),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassIconButton(
                    painter = painterResource(Res.drawable.ic_tune),
                    contentDescription = "Фильтр",
                    onClick = { /* TODO: open filter */ },
                )
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
                    applyInsets = false,
                )
                GlassIconButton(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = "Поиск",
                    onClick = { /* TODO: open search */ },
                )
            }
        }
    }
}
