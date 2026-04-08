package com.ownstd.project.core.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

private val BUTTON_CORNER = 8.dp

@Composable
fun SizeSelector(
    sizes: List<String>,
    selectedSize: String?,
    onSizeSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier,
    ) {
        items(sizes) { size ->
            val isSelected = size == selectedSize
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(BUTTON_CORNER))
                    .background(
                        if (isSelected) Theme.colors.accent.primary
                        else Theme.colors.background.surface,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Theme.colors.accent.primary else Theme.colors.stroke.primary,
                        shape = RoundedCornerShape(BUTTON_CORNER),
                    )
                    .clickable { onSizeSelect(size) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = size,
                    style = Theme.typography.label,
                    color = if (isSelected) Theme.colors.accent.onAccent else Theme.colors.label.primary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SizeSelectorPreview() {
    SizeSelector(
        sizes = listOf("XXS", "XS", "S", "M", "L", "XL", "XXL"),
        selectedSize = "M",
        onSizeSelect = {},
    )
}
