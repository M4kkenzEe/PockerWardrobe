package com.ownstd.project.card.internal.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ownstd.project.authorization.internal.presentation.AuthorizationScreen
import com.ownstd.project.card.internal.presentation.screens.MainScreen

@Composable
internal fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Authorization,
        modifier = modifier.fillMaxSize()
    ) {
        composable<AppScreens.Authorization> {
            AuthorizationScreen(
                openSession = {
                    navController.navigate(AppScreens.Main) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<AppScreens.Main> {
            MainScreen(navController = rememberNavController())
        }
    }
}