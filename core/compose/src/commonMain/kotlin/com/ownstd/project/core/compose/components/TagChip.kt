package com.ownstd.project.core.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Theme.colors.component.tag.background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = Theme.typography.tiny,
            color = Theme.colors.component.tag.foreground,
        )
    }
}

@Preview
@Composable
private fun TagChipPreview() {
    TagChip(text = "Верх")
}
