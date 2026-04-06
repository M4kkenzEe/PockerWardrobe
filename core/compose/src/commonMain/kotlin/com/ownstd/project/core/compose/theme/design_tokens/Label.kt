package com.ownstd.project.core.compose.theme.design_tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.colorResource

/**
 * Цвета текста и надписей.
 *
 * Используй [primary] для заголовков и основного текста,
 * [secondary] для вторичного текста (подзаголовки, описания),
 * [muted] для приглушённого текста (плейсхолдеры, подсказки, неактивные элементы).
 */
class Label(private val isDark: Boolean) {

    /** Заголовки, основной текст, важные надписи. Light: #1A1A1A / Dark: #EFEFEF */
    val primary: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorText_dark) else colorResource(MR.colors.colorText_light)

    /** Подзаголовки, описания, вторичная информация. Light: #7A7A7A / Dark: #606060 */
    val secondary: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorTextSub_dark) else colorResource(MR.colors.colorTextSub_light)

    /** Плейсхолдеры, подсказки, неактивный текст. Light: #B0B0B0 / Dark: #333333 */
    val muted: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorTextMuted_dark) else colorResource(MR.colors.colorTextMuted_light)

    /** Текст ошибок, валидация. #D4183D */
    val error: Color
        @Composable get() = colorResource(MR.colors.colorError)

    /** Текст на фоне ошибки (кнопка «Удалить» и т.п.). #FFFFFF */
    val onError: Color
        @Composable get() = colorResource(MR.colors.colorErrorFg)
}
