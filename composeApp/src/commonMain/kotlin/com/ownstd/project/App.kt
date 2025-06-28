package com.ownstd.project

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.ownstd.project.card.api.RootScreen
import com.ownstd.project.card.internal.di.cardModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin

@Composable
@Preview
fun App() {
    initKoin()
    MaterialTheme {
        RootScreen()
    }
}

fun initKoin() {
    startKoin {
        modules(cardModule)
    }
}