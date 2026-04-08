package com.ownstd.project.core.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingRow(
    title: String,
    icon: ImageResource,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showDivider: Boolean = true,
    onClick: () -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Theme.colors.label.secondary,
                modifier = Modifier.size(20.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = Theme.typography.body,
                    color = Theme.colors.label.primary,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = Theme.typography.caption,
                        color = Theme.colors.label.secondary,
                    )
                }
            }

            Icon(
                painter = painterResource(MR.images.chevron_right),
                contentDescription = null,
                tint = Theme.colors.label.muted,
                modifier = Modifier.size(16.dp),
            )
        }

        if (showDivider) {
            Divider(
                color = Theme.colors.stroke.primary,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 48.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SettingRowPreview() {
    SettingRow(
        title = "Размеры",
        subtitle = "EU 42 · S",
        icon = MR.images.chevron_right,
        onClick = {},
    )
}
