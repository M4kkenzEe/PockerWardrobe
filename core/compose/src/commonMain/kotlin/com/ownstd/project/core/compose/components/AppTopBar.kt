package com.ownstd.project.core.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val TOP_BAR_HEIGHT = 56.dp
private val BACK_BUTTON_SIZE = 36.dp

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TOP_BAR_HEIGHT)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(BACK_BUTTON_SIZE)
                        .clip(CircleShape)
                        .background(Theme.colors.background.surface),
                ) {
                    Icon(
                        painter = painterResource(MR.images.chevron_left),
                        contentDescription = "Назад",
                        tint = Theme.colors.label.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(BACK_BUTTON_SIZE))
            }

            Text(
                text = title,
                style = Theme.typography.body,
                color = Theme.colors.label.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier.size(BACK_BUTTON_SIZE),
                contentAlignment = Alignment.Center,
            ) {
                trailingContent?.invoke()
            }
        }

        Divider(
            color = Theme.colors.stroke.primary,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
    }
}

@Preview
@Composable
private fun AppTopBarPreview() {
    AppTopBar(
        title = "Гардероб",
        onBack = {},
    )
}

@Preview
@Composable
private fun AppTopBarWithTrailingPreview() {
    AppTopBar(
        title = "Вещь",
        onBack = {},
        trailingContent = {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(MR.images.pencil),
                    contentDescription = "Редактировать",
                    tint = Theme.colors.label.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    )
}
