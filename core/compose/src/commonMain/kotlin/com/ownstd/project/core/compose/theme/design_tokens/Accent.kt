package com.ownstd.project.core.compose.theme.design_tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.colorResource

/**
 * Акцентные цвета — кнопки, выделения, активные состояния.
 *
 * Используй [primary] для фона primary-кнопок, FAB, активных чипсов,
 * [onAccent] для текста/иконок поверх акцента,
 * [subtle] для мягкого фона акцента (бейджи «3 выбрано», фон акцентного блока).
 */
class Accent(private val isDark: Boolean) {

    /** Фон primary-кнопок, активных чипсов, выделений. Light: #1A1A1A / Dark: #EFEFEF */
    val primary: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorAccent_dark) else colorResource(MR.colors.colorAccent_light)

    /** Текст и иконки поверх акцента. Light: #FFFFFF / Dark: #080808 */
    val onAccent: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorAccentFg_dark) else colorResource(MR.colors.colorAccentFg_light)

    /** Мягкий фон акцента — бейджи, subtle-кнопки, фон счётчиков. Light: #F0F0EE / Dark: #1A1A1A */
    val subtle: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorAccentSubtle_dark) else colorResource(MR.colors.colorAccentSubtle_light)
}
