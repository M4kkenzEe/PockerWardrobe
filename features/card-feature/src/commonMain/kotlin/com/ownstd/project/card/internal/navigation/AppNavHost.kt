package com.ownstd.project.card.internal.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ownstd.project.authorization.internal.presentation.AuthorizationScreen
import com.ownstd.project.card.internal.presentation.screens.MainScreen
import com.ownstd.project.storage.TokenStorage
import org.koin.compose.koinInject

@Composable
internal fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val tokenStorage: TokenStorage = koinInject()

    val token = tokenStorage.getAccessToken()
    val isAuthorized = !token.isNullOrEmpty()

    val startDestination = if (isAuthorized) {
        AppScreens.Main
    } else {
        AppScreens.Authorization
    }

    HandleDeepLink(navController)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        composable<AppScreens.Authorization> {
            AuthorizationScreen(
                openSession = {
                    navController.navigate(AppScreens.Main) {
                        popUpTo(AppScreens.Authorization) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<AppScreens.Main> {
            MainScreen(parentNavController = navController)
        }
    }
}
