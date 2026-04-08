package com.ownstd.project.wardrobe.internal.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.components.AppBottomSheet
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.wardrobe.internal.domain.model.FilterOptions
import com.ownstd.project.wardrobe.internal.domain.model.SortOrder
import org.jetbrains.compose.ui.tooling.preview.Preview

private val SEASONS = listOf("Весна", "Лето", "Осень", "Зима")

private val SORT_OPTIONS = listOf(
    SortOrder.BY_DATE to "По дате",
    SortOrder.BY_NAME to "По имени",
    SortOrder.BY_CATEGORY to "По категории",
)

private data class ColorOption(val name: String, val value: Color)

private val COLOR_OPTIONS = listOf(
    ColorOption("Белый", Color(0xFFFFFFFF)),
    ColorOption("Чёрный", Color(0xFF1A1A1A)),
    ColorOption("Серый", Color(0xFF808080)),
    ColorOption("Красный", Color(0xFFE53935)),
    ColorOption("Синий", Color(0xFF1E88E5)),
    ColorOption("Зелёный", Color(0xFF43A047)),
    ColorOption("Жёлтый", Color(0xFFFDD835)),
    ColorOption("Оранжевый", Color(0xFFFB8C00)),
    ColorOption("Коричневый", Color(0xFF6D4C41)),
    ColorOption("Розовый", Color(0xFFEC407A)),
    ColorOption("Фиолетовый", Color(0xFF8E24AA)),
    ColorOption("Бежевый", Color(0xFFD7CCC8)),
)

@Composable
internal fun FilterBottomSheet(
    visible: Boolean,
    currentOptions: FilterOptions,
    onApply: (FilterOptions) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(visible) { mutableStateOf(currentOptions) }

    AppBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = "Фильтр",
    ) {
        FilterSection(title = "Сортировка") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(SORT_OPTIONS) { (order, label) ->
                    FilterChip(
                        label = label,
                        isActive = draft.sortOrder == order,
                        onClick = { draft = draft.copy(sortOrder = order) },
                    )
                }
            }
        }

        FilterSection(title = "Сезон") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(SEASONS) { season ->
                    FilterChip(
                        label = season,
                        isActive = season in draft.seasons,
                        onClick = {
                            draft = draft.copy(
                                seasons = if (season in draft.seasons)
                                    draft.seasons - season
                                else
                                    draft.seasons + season,
                            )
                        },
                    )
                }
            }
        }

        FilterSection(title = "Цвет") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(COLOR_OPTIONS) { option ->
                    ColorCircle(
                        option = option,
                        isSelected = option.name in draft.colors,
                        onClick = {
                            draft = draft.copy(
                                colors = if (option.name in draft.colors)
                                    draft.colors - option.name
                                else
                                    draft.colors + option.name,
                            )
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (draft.isActive) {
                TextButton(
                    onClick = { draft = FilterOptions() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Сбросить",
                        style = Theme.typography.body,
                        color = Theme.colors.label.secondary,
                    )
                }
            }
            Button(
                onClick = { onApply(draft) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Theme.colors.accent.primary,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(if (draft.isActive) 2f else 1f),
            ) {
                Text(
                    text = "Показать",
                    style = Theme.typography.body,
                    color = Theme.colors.accent.onAccent,
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = Theme.typography.label,
            color = Theme.colors.label.secondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
        content()
    }
}

@Composable
private fun FilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isActive) Theme.colors.component.chip.activeBackground
                else Theme.colors.component.chip.background,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = Theme.typography.label,
            color = if (isActive) Theme.colors.component.chip.activeForeground
            else Theme.colors.component.chip.foreground,
        )
    }
}

@Composable
private fun ColorCircle(
    option: ColorOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(option.value)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Theme.colors.accent.primary else Theme.colors.stroke.primary,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}

@Preview
@Composable
private fun FilterBottomSheetPreview() {
    FilterBottomSheet(
        visible = true,
        currentOptions = FilterOptions(seasons = listOf("Лето")),
        onApply = {},
        onDismiss = {},
    )
}
