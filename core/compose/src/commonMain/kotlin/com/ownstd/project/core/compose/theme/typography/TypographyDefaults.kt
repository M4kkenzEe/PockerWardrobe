package com.ownstd.project.core.compose.theme.typography

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.asFont

@Composable
fun interFontFamily(): FontFamily = FontFamily(
    MR.fonts.inter_thin.asFont(weight = FontWeight.W100)!!,
    MR.fonts.inter_light.asFont(weight = FontWeight.W300)!!,
    MR.fonts.inter_regular.asFont(weight = FontWeight.W400)!!,
    MR.fonts.inter_medium.asFont(weight = FontWeight.W500)!!,
    MR.fonts.inter_semibold.asFont(weight = FontWeight.W600)!!,
    MR.fonts.inter_bold.asFont(weight = FontWeight.W700)!!,
)

@Composable
fun defaultTypography(): ProjectTypography {
    val fontFamily = interFontFamily()
    return ProjectTypography(
        h1 = TextStyle(
            fontFamily = fontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.W700,
            lineHeight = 28.sp,
        ),
        h2 = TextStyle(
            fontFamily = fontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.W700,
            lineHeight = 26.sp,
        ),
        h3 = TextStyle(
            fontFamily = fontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.W700,
            lineHeight = 22.sp,
        ),
        subhead = TextStyle(
            fontFamily = fontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.W600,
            lineHeight = 20.sp,
        ),
        body = TextStyle(
            fontFamily = fontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.W500,
            lineHeight = 18.sp,
        ),
        label = TextStyle(
            fontFamily = fontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.W500,
            lineHeight = 17.sp,
        ),
        caption = TextStyle(
            fontFamily = fontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.W400,
            lineHeight = 16.sp,
        ),
        small = TextStyle(
            fontFamily = fontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.W500,
            lineHeight = 14.sp,
        ),
        micro = TextStyle(
            fontFamily = fontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.W500,
            lineHeight = 13.sp,
        ),
        tiny = TextStyle(
            fontFamily = fontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.W600,
            lineHeight = 12.sp,
        ),
    )
}
