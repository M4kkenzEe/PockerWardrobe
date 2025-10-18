package com.ownstd.project.card.internal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/**
 * Handles deep links by observing DeepLinkManager and navigating accordingly
 * This is a platform-agnostic composable that works on both Android and iOS
 */
@Composable
internal expect fun HandleDeepLink(navController: NavHostController)
