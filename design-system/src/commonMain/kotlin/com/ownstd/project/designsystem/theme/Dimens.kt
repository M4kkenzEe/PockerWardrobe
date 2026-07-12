package com.ownstd.project.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ClothisDimens(
    // Spacing scale — 4pt base
    val spaceXs: Dp = 4.dp,
    val spaceSm: Dp = 8.dp,
    val spaceMd: Dp = 12.dp,
    val spaceLg: Dp = 16.dp,
    val spaceXl: Dp = 20.dp,
    val space2xl: Dp = 24.dp,
    val space3xl: Dp = 32.dp,
    val space4xl: Dp = 40.dp,

    // Screen layout
    val screenHorizontalPadding: Dp = 20.dp,
    val gridGutter: Dp = 12.dp,

    // Radius tokens
    val radiusChip: Dp = 12.dp,
    val radiusButton: Dp = 16.dp,
    val radiusCard: Dp = 20.dp,
    val radiusSheet: Dp = 24.dp,
    val radiusTile: Dp = 28.dp,
    val radiusFull: Dp = 999.dp,

    // Component sizes
    val buttonHeight: Dp = 52.dp,
    val iconButtonSize: Dp = 40.dp,
    val iconButtonIcon: Dp = 24.dp,
    val segmentControlHeight: Dp = 40.dp,
    val segmentControlWidth: Dp = 192.dp,

    // Empty state
    val emptyStateTileSize: Dp = 128.dp,
    val emptyStateTileIconSize: Dp = 56.dp,
    val emptyStateMaxWidth: Dp = 280.dp,

    // Nav island
    val navIslandHeight: Dp = 64.dp,
    val navIslandDotSize: Dp = 4.dp,
    val navIslandBottomGap: Dp = 12.dp,
    val navIslandIconSize: Dp = 28.dp,
)

internal val LocalClothisDimens = staticCompositionLocalOf { ClothisDimens() }
