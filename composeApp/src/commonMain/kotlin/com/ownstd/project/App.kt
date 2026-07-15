package com.ownstd.project

import androidx.compose.runtime.Composable
import com.ownstd.project.card.api.RootScreen
import com.ownstd.project.card.internal.di.cardModule
import com.ownstd.project.designsystem.theme.ClothisTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

@Composable
@Preview
fun App() {
    initKoin()
    ClothisTheme {
        RootScreen()
    }
}

fun initKoin() {
    try {
        KoinPlatform.getKoin()
    } catch (e: Exception) {
        startKoin {
            modules(cardModule)
        }
    }
}
