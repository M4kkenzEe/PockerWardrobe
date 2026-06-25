package com.ownstd.project.card.api

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.compose.rememberNavController
import com.ownstd.project.card.internal.navigation.AppNavHost
import com.ownstd.project.card.internal.navigation.AppScreens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEBUG_HOLD_MS = 5_000L

@Composable
fun RootScreen() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    var debugTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(debugTriggered) {
        if (debugTriggered) {
            navController.navigate(AppScreens.Debug) { launchSingleTop = true }
            debugTriggered = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(coroutineScope) {
                awaitPointerEventScope {
                    while (true) {
                        // Initial pass — observe before children handle the event
                        val down = awaitPointerEvent(PointerEventPass.Initial)
                        if (down.changes.none { it.pressed }) continue

                        val job = coroutineScope.launch {
                            delay(DEBUG_HOLD_MS)
                            debugTriggered = true
                        }

                        // Wait until all fingers are lifted
                        while (true) {
                            val up = awaitPointerEvent(PointerEventPass.Initial)
                            if (up.changes.none { it.pressed }) {
                                job.cancel()
                                break
                            }
                        }
                    }
                }
            }
    ) {
        AppNavHost(navController)
    }
}
