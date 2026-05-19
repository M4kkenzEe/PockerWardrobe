package com.ownstd.project.main

import androidx.navigation3.runtime.NavKey

object AppRouteManager {
    fun replaceWith(backStack: MutableList<NavKey>, route: NavKey) {
        while (backStack.isNotEmpty()) backStack.removeAt(backStack.lastIndex)
        backStack.add(route)
    }
}
