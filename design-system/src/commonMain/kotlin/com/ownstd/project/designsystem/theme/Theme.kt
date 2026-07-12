package com.ownstd.project.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ClothisColors(
    val canvas: Color,
    val surface: Color,
    val clay: Color,
    val teal: Color,
    val onCanvas: Color,
    val onCanvasMuted: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val onClay: Color,
    val onTeal: Color,
    val divider: Color,
    val surfaceDivider: Color,
    val surfaceElevated: Color,
    val segmentTrack: Color,
    val segmentActive: Color,
    val ghostSurface: Color,
    val glassTint: Color,
    val glassStroke: Color,
    val error: Color,
)

private val defaultColors = ClothisColors(
    canvas = Canvas,
    surface = Surface,
    clay = Clay,
    teal = Teal,
    onCanvas = OnCanvas,
    onCanvasMuted = OnCanvasMuted,
    onSurface = OnSurface,
    onSurfaceMuted = OnSurfaceMuted,
    onClay = OnClay,
    onTeal = OnTeal,
    divider = Divider,
    surfaceDivider = SurfaceDivider,
    surfaceElevated = SurfaceElevated,
    segmentTrack = SegmentTrack,
    segmentActive = SegmentActive,
    ghostSurface = GhostSurface,
    glassTint = GlassTint,
    glassStroke = GlassStroke,
    error = ErrorColor,
)

private val LocalClothisColors = staticCompositionLocalOf { defaultColors }

object ClothisTheme {
    val colors: ClothisColors
        @Composable get() = LocalClothisColors.current

    val typography: ClothisTypography
        @Composable get() = LocalClothisTypography.current

    val dimens: ClothisDimens
        @Composable get() = LocalClothisDimens.current
}

@Composable
fun ClothisTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalClothisColors provides defaultColors,
        LocalClothisTypography provides ClothisTypography(),
        LocalClothisDimens provides ClothisDimens(),
        content = content,
    )
}
