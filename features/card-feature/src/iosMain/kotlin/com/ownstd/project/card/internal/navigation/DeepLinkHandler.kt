package com.ownstd.project.card.internal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.ownstd.project.card.internal.deeplink.DeepLink
import com.ownstd.project.card.internal.deeplink.getDeepLinkManager

@Composable
internal actual fun HandleDeepLink(navController: NavHostController) {
    val deepLinkManager = getDeepLinkManager()
    val deepLink: DeepLink? by deepLinkManager.deepLinkFlow.collectAsState(null)

    LaunchedEffect(deepLink) {
        deepLink?.let { link ->
            println("iOS DeepLinkHandler: Handling deep link: $link")

            // Deep link navigation is now handled in MainScreen via dynamic startDestination
            // Deep link will be consumed by BottomNavigationNavHost and cleared by MainScreen
            when (link) {
                is DeepLink.Look -> {
                    println("iOS DeepLinkHandler: Deep link detected with shareToken: ${link.shareToken}")
                    println("iOS DeepLinkHandler: Deep link will be used as startDestination in BottomNavigationNavHost")
                    // Deep link stays active and will be consumed by MainScreen/BottomNavigationNavHost
                    // MainScreen will clear the deep link after using it
                }
            }
        }
    }
}
