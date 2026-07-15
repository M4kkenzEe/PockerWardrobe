package com.ownstd.project.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// TODO: Bundle Manrope .ttf files in composeResources/font/ and replace FontFamily.Default here.
// Weights needed: Regular(400), Medium(500), SemiBold(600), Bold(700).
private val Manrope = FontFamily.Default

data class ClothisTypography(
    val h1: TextStyle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).em,
    ),
    val h2: TextStyle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).em,
    ),
    val title: TextStyle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight(600),
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    val body: TextStyle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    val label: TextStyle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = (0.06).em,
    ),
)

internal val LocalClothisTypography = staticCompositionLocalOf { ClothisTypography() }
