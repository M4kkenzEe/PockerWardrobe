package com.ownstd.project.core.compose.theme.design_tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.colorResource

/**
 * Цвета конкретных UI-компонентов: чипсы, теги, навигация, FAB, скелетон.
 */
class Component(private val isDark: Boolean) {
    val chip: Chip = Chip(isDark)
    val tag: Tag = Tag(isDark)
    val nav: Nav = Nav(isDark)
    val fab: Fab = Fab(isDark)

    /** Фон скелетон-заглушек при загрузке контента. Light: #EFEFED / Dark: #1A1A1A */
    val skeleton: Color
        @Composable get() = if (isDark) colorResource(MR.colors.colorSkeleton_dark) else colorResource(MR.colors.colorSkeleton_light)

    /**
     * Чипсы фильтров и категорий (Casual, Sport, Evening).
     * [background]/[foreground] — неактивное состояние,
     * [activeBackground]/[activeForeground] — выбранный чипс.
     */
    class Chip(private val isDark: Boolean) {
        /** Фон неактивного чипса. Light: #F0F0EE / Dark: #181818 */
        val background: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorChip_dark) else colorResource(MR.colors.colorChip_light)

        /** Текст неактивного чипса. Light: #7A7A7A / Dark: #606060 */
        val foreground: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorChipFg_dark) else colorResource(MR.colors.colorChipFg_light)

        /** Фон выбранного чипса. Light: #1A1A1A / Dark: #EFEFEF */
        val activeBackground: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorChipActive_dark) else colorResource(MR.colors.colorChipActive_light)

        /** Текст выбранного чипса. Light: #FFFFFF / Dark: #080808 */
        val activeForeground: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorChipActiveFg_dark) else colorResource(MR.colors.colorChipActiveFg_light)
    }

    /**
     * Теги категорий одежды (Верх, Обувь, Сумки).
     */
    class Tag(private val isDark: Boolean) {
        /** Фон тега. Light: #F0F0EE / Dark: #1A1A1A */
        val background: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorTag_dark) else colorResource(MR.colors.colorTag_light)

        /** Текст тега. Light: #7A7A7A / Dark: #606060 */
        val foreground: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorTagFg_dark) else colorResource(MR.colors.colorTagFg_light)
    }

    /**
     * Нижняя навигационная панель (BottomNavigationBar).
     */
    class Nav(private val isDark: Boolean) {
        /** Фон панели навигации. Light: #FFFFFF / Dark: #080808 */
        val background: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorNav_dark) else colorResource(MR.colors.colorNav_light)

        /** Верхняя граница панели. Light: #F0F0EE / Dark: #181818 */
        val border: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorNavBorder_dark) else colorResource(MR.colors.colorNavBorder_light)

        /** Иконка/текст активной вкладки. Light: #1A1A1A / Dark: #EFEFEF */
        val active: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorNavActive_dark) else colorResource(MR.colors.colorNavActive_light)

        /** Иконка/текст неактивной вкладки. Light: #C8C8C8 / Dark: #363636 */
        val inactive: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorNavInactive_dark) else colorResource(MR.colors.colorNavInactive_light)
    }

    /**
     * Плавающая кнопка действия (FloatingActionButton) — «Создать образ», «+».
     */
    class Fab(private val isDark: Boolean) {
        /** Фон FAB. Light: #1A1A1A / Dark: #EFEFEF */
        val background: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorFab_dark) else colorResource(MR.colors.colorFab_light)

        /** Иконка на FAB. Light: #FFFFFF / Dark: #080808 */
        val foreground: Color
            @Composable get() = if (isDark) colorResource(MR.colors.colorFabFg_dark) else colorResource(MR.colors.colorFabFg_light)
    }
}
