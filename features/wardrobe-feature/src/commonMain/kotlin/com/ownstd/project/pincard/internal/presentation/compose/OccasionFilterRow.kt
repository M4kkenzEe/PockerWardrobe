package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class FilterChip(val label: String, val value: String?)

private val FILTER_CHIPS = listOf(
    FilterChip("Все", null),
    FilterChip("Повседневное", "casual"),
    FilterChip("Офис", "office"),
    FilterChip("Спорт", "sport"),
    FilterChip("Свидание", "date"),
    FilterChip("Путешествие", "travel"),
)

@Composable
internal fun OccasionFilterRow(
    selectedOccasion: String?,
    onFilterSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(FILTER_CHIPS) { chip ->
            val isSelected = chip.value == selectedOccasion
            val shape = RoundedCornerShape(20.dp)
            Surface(
                color = if (isSelected) Color(0xFF8AB4F8) else Color(0xFF2E2E2E),
                shape = shape,
                modifier = Modifier
                    .clip(shape)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF8AB4F8) else Color(0xFF3E3E3E),
                        shape = shape
                    )
                    .clickable { onFilterSelected(chip.value) }
            ) {
                Text(
                    text = chip.label,
                    color = if (isSelected) Color(0xFF27272B) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}
