package com.ownstd.project.core.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CategoryChips(
    categories: List<String>,
    activeCategory: String?,
    onCategorySelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Int = 16,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = horizontalPadding.dp),
        modifier = modifier,
    ) {
        items(categories) { category ->
            val isAll = category == categories.first()
            val isActive = if (isAll) activeCategory == null else category == activeCategory
            CategoryChipItem(
                label = category,
                isActive = isActive,
                onClick = { onCategorySelect(if (isAll) null else category) },
            )
        }
    }
}

@Composable
private fun CategoryChipItem(
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

@Preview
@Composable
private fun CategoryChipsPreview() {
    CategoryChips(
        categories = listOf("Все", "Верх", "Низ", "Обувь", "Верхняя", "Сумки"),
        activeCategory = "Верх",
        onCategorySelect = {},
    )
}
