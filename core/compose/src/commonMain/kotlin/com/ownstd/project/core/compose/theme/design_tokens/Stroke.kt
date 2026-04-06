package com.ownstd.project.core.compose.theme.design_tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.colorResource

/**
 * Цвета обводок, разделителей и границ.
 *
 * Используй [primary] для бордеров карточек, разделителей списков,
 * обводок secondary-кнопок, линий между секциями.
 */
class Stroke(private val isDark: Boolean) {

    /** Бордеры карточек, разделители, обводки кнопок. Light: #E8E8E5 / Dark: #1E1E1E */
    val primary: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorBorder_dark) else colorResource(MR.colors.colorBorder_light)
}
