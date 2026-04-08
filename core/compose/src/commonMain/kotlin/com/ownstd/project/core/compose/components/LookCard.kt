package com.ownstd.project.core.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val CORNER_RADIUS = 16.dp

@Composable
fun LookCard(
    imageUrl: String,
    name: String,
    isLocked: Boolean = false,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    menuVisible: Boolean = false,
    onMenuDismiss: () -> Unit = {},
    menuItems: List<MoreMenuItem> = emptyList(),
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(Theme.colors.background.surface)
            .clickable(enabled = !isLocked, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Theme.colors.background.surfaceAlt)
                    .then(if (isLocked) Modifier.blur(12.dp) else Modifier),
            )

            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(MR.images.lock),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = name,
                style = Theme.typography.caption,
                color = Theme.colors.label.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            MoreMenu(
                visible = menuVisible,
                items = menuItems,
                onDismiss = onMenuDismiss,
                anchor = {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painter = painterResource(MR.images.more_horizontal),
                            contentDescription = null,
                            tint = Theme.colors.label.secondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun LookCardPreview() {
    LookCard(
        imageUrl = "",
        name = "Офисный образ",
        onClick = {},
        onMoreClick = {},
    )
}

@Preview
@Composable
private fun LookCardLockedPreview() {
    LookCard(
        imageUrl = "",
        name = "Вечерний образ",
        isLocked = true,
        onClick = {},
        onMoreClick = {},
    )
}
