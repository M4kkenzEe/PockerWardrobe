package com.ownstd.project.core.compose.theme.typography

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Stable
class ProjectTypography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val subhead: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val small: TextStyle,
    val micro: TextStyle,
    val tiny: TextStyle,
)

val LocalTypography = staticCompositionLocalOf {
    ProjectTypography(
        h1 = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.W700),
        h2 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W700),
        h3 = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.W700),
        subhead = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.W600),
        body = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W500),
        label = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.W500),
        caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W400),
        small = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.W500),
        micro = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.W500),
        tiny = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.W600),
    )
}
