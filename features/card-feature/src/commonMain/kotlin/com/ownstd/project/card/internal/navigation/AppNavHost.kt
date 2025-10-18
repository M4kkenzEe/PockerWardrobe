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

    // Проверяем авторизацию при создании NavHost
    // Проверка выполняется только один раз при инициализации для определения startDestination
    val token = tokenStorage.getToken()
    val isAuthorized = !token.isNullOrEmpty()

    // Динамически определяем startDestination в зависимости от авторизации
    val startDestination = if (isAuthorized) {
        AppScreens.Main
    } else {
        AppScreens.Authorization
    }

    // Handle deep links from DeepLinkManager
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
                        // Удаляем Authorization из стека
                        popUpTo(AppScreens.Authorization) {
                            inclusive = true
                        }
                        // Предотвращаем создание дубликата Main, если он уже есть
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