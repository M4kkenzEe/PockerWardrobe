package com.ownstd.project.core.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = Theme.typography.h3,
            color = Theme.colors.label.primary,
        )
        Text(
            text = label,
            style = Theme.typography.caption,
            color = Theme.colors.label.secondary,
        )
    }
}

@Composable
fun StatsRow(
    outfitsCount: Int,
    clothesCount: Int,
    sharedCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatItem(
            value = outfitsCount.toString(),
            label = "Образов",
            modifier = Modifier.weight(1f),
        )
        Divider(
            color = Theme.colors.stroke.primary,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
        )
        StatItem(
            value = clothesCount.toString(),
            label = "Вещей",
            modifier = Modifier.weight(1f),
        )
        Divider(
            color = Theme.colors.stroke.primary,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
        )
        StatItem(
            value = sharedCount.toString(),
            label = "Поделились",
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun StatsRowPreview() {
    StatsRow(outfitsCount = 42, clothesCount = 167, sharedCount = 28)
}
