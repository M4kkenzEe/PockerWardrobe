package com.ownstd.project.card.api

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.ownstd.project.card.internal.navigation.AppNavHost

@Composable
fun RootScreen() {
    val navController = rememberNavController()
    AppNavHost(navController)
}