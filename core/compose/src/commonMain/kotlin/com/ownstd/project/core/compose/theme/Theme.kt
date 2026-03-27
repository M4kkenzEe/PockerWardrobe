package com.ownstd.project.core.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.ownstd.project.core.compose.theme.typography.LocalTypography
import com.ownstd.project.core.compose.theme.typography.ProjectTypography
import com.ownstd.project.core.compose.theme.typography.defaultTypography

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColorProvider provides if (darkTheme) darkPalette else lightPalette,
        LocalTypography provides defaultTypography(),
        content = content,
    )
}

object Theme {
    val colors: ProjectColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColorProvider.current

    val typography: ProjectTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
}
