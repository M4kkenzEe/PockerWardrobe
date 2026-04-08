package com.ownstd.project.core.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

data class MoreMenuItem(
    val icon: ImageResource,
    val label: String,
    val isDestructive: Boolean = false,
    val action: () -> Unit,
)

@Composable
fun MoreMenu(
    visible: Boolean,
    items: List<MoreMenuItem>,
    onDismiss: () -> Unit,
    anchor: @Composable () -> Unit,
) {
    Box {
        anchor()
        DropdownMenu(
            expanded = visible,
            onDismissRequest = onDismiss,
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    onClick = {
                        item.action()
                        onDismiss()
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = null,
                            tint = if (item.isDestructive) Theme.colors.label.error
                            else Theme.colors.label.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item.label,
                            style = Theme.typography.body,
                            color = if (item.isDestructive) Theme.colors.label.error
                            else Theme.colors.label.primary,
                        )
                    }
                }
                if (index < items.size - 1) {
                    Divider(color = Theme.colors.stroke.primary)
                }
            }
        }
    }
}

@Preview
@Composable
private fun MoreMenuPreview() {
    MoreMenu(
        visible = true,
        onDismiss = {},
        items = listOf(
            MoreMenuItem(MR.images.bookmark, "В избранное") {},
            MoreMenuItem(MR.images.share_2, "Поделиться") {},
            MoreMenuItem(MR.images.trash_2, "Удалить", isDestructive = true) {},
        ),
        anchor = {},
    )
}
