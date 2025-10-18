package com.ownstd.project.card.internal.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.ownstd.project.card.internal.deeplink.DeepLink
import com.ownstd.project.card.internal.deeplink.DeepLinkManager
import com.ownstd.project.storage.TokenStorage
import org.koin.compose.koinInject

@Composable
internal actual fun HandleDeepLink(navController: NavHostController) {
    val deepLinkManager = DeepLinkManager.getInstance()
    val deepLink by deepLinkManager.deepLinkFlow.collectAsState()
    val pendingDeepLink by deepLinkManager.pendingDeepLink.collectAsState()
    val tokenStorage: TokenStorage = koinInject()

    // Handle new deep links
    LaunchedEffect(deepLink) {
        deepLink?.let { link ->
            Log.d("DeepLinkHandler", "Handling deep link: $link")

            // Check if user is authorized
            val token = tokenStorage.getToken()
            val isAuthorized = !token.isNullOrEmpty()

            when (link) {
                is DeepLink.Look -> {
                    if (isAuthorized) {
                        Log.d("DeepLinkHandler", "User is authorized, deep link will be used as startDestination in BottomNavigationNavHost")
                        // User is already authorized
                        // Deep link stays active and will be consumed by MainScreen/BottomNavigationNavHost as startDestination
                        // MainScreen will clear the deep link after using it
                    } else {
                        Log.d("DeepLinkHandler", "User is not authorized, saving as pending deep link")
                        // User is not authorized - save as pending
                        deepLinkManager.setPendingDeepLink(link)
                        deepLinkManager.clearDeepLink()
                    }
                }
            }
        }
    }

    // Handle pending deep links after authorization
    LaunchedEffect(pendingDeepLink) {
        val token = tokenStorage.getToken()
        if (!token.isNullOrEmpty() && pendingDeepLink != null) {
            Log.d("DeepLinkHandler", "Applying pending deep link after authorization: $pendingDeepLink")

            // Применяем pending deep link
            deepLinkManager.applyPendingDeepLink()

            // Проверяем, находимся ли мы на Authorization экране
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            Log.d("DeepLinkHandler", "Current route: $currentRoute")

            if (currentRoute == AppScreens.Authorization::class.qualifiedName) {
                Log.d("DeepLinkHandler", "Navigating from Authorization to Main after pending deep link")
                navController.navigate(AppScreens.Main) {
                    popUpTo(AppScreens.Authorization) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}
