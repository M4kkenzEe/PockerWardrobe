package com.ownstd.project

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.ownstd.project.card.api.MainScreen
import com.ownstd.project.card.internal.di.cardModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin

@Composable
@Preview
fun App() {
    initKoin()
    MaterialTheme {
        MainScreen()
    }
}

fun initKoin() {
    startKoin {
        modules(cardModule)
    }
}