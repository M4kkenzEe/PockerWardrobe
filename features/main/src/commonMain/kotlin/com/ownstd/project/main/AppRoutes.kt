package com.ownstd.project.main

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoutes : NavKey {
    @Serializable data object Auth : AppRoutes()
    @Serializable data object Main : AppRoutes()
}
