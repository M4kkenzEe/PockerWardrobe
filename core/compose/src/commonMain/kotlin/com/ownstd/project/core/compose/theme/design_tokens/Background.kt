package com.ownstd.project.core.compose.theme.design_tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.colorResource

/**
 * Фоновые цвета приложения.
 *
 * Используй [primary] для основного фона экранов,
 * [surface] для карточек и модальных окон,
 * [surfaceAlt] для альтернативных поверхностей (заголовки карточек, вложенные блоки).
 */
class Background(private val isDark: Boolean) {

    /** Основной фон экрана. Light: #F4F4F4 / Dark: #080808 */
    val primary: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorBackground_dark) else colorResource(MR.colors.colorBackground_light)

    /** Карточки, диалоги, bottom sheet. Light: #F5F5F2 / Dark: #111111 */
    val surface: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorSurface_dark) else colorResource(MR.colors.colorSurface_light)

    /** Вложенные блоки внутри карточек, заголовки карточек. Light: #EEEEEB / Dark: #181818 */
    val surfaceAlt: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorSurfaceAlt_dark) else colorResource(MR.colors.colorSurfaceAlt_light)
}
