package com.ownstd.project

import androidx.compose.runtime.Composable
import com.ownstd.project.core.compose.theme.AppTheme
import com.ownstd.project.core.mockdata.AppConfig
import com.ownstd.project.core.mockdata.mockModule
import com.ownstd.project.main.AppNavHost
import com.ownstd.project.main.di.mainModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

@Composable
@Preview
fun App() {
    initKoin()
    AppTheme {
        AppNavHost()
    }
}

fun initKoin() {
    try {
        KoinPlatform.getKoin()
    } catch (e: Exception) {
        startKoin {
            allowOverride(AppConfig.USE_MOCK_DATA)
            modules(mainModule)
            if (AppConfig.USE_MOCK_DATA) modules(mockModule)
        }
    }
}
