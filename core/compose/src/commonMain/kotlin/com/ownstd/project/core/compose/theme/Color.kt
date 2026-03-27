package com.ownstd.project.core.compose.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.ownstd.project.core.compose.theme.design_tokens.Accent
import com.ownstd.project.core.compose.theme.design_tokens.Background
import com.ownstd.project.core.compose.theme.design_tokens.Component
import com.ownstd.project.core.compose.theme.design_tokens.Label
import com.ownstd.project.core.compose.theme.design_tokens.Stroke

/**
 * Все цвета проекта, сгруппированные по семантическому назначению.
 *
 * Доступ: `Theme.colors.background.primary`, `Theme.colors.label.secondary` и т.д.
 *
 * @param isDark true для Dark Premium темы, false для Light Minimalism
 */
class ProjectColors(isDark: Boolean) {
    /** Фоны экранов, карточек, поверхностей */
    val background: Background = Background(isDark)

    /** Цвета текста и надписей */
    val label: Label = Label(isDark)

    /** Акцентные цвета — кнопки, выделения */
    val accent: Accent = Accent(isDark)

    /** Обводки, разделители, границы */
    val stroke: Stroke = Stroke(isDark)

    /** Цвета конкретных компонентов: чипсы, теги, навигация, FAB, скелетон */
    val component: Component = Component(isDark)
}

/** Light Minimalism — Apple-style, Studio vibe */
val lightPalette = ProjectColors(false)

/** Dark Premium — Editorial, Fashion vibe */
val darkPalette = ProjectColors(true)

val LocalColorProvider = staticCompositionLocalOf { lightPalette }
