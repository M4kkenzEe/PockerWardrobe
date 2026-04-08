package com.ownstd.project.core.compose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val CORNER_RADIUS = 16.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    imageUrl: String,
    name: String,
    category: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(CORNER_RADIUS))
                .background(Theme.colors.background.surface)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        ) {
            Box {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Theme.colors.background.surfaceAlt),
                )
                if (selectionMode) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(22.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Theme.colors.accent.primary
                                else Theme.colors.background.primary,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(MR.images.check),
                                contentDescription = null,
                                tint = Theme.colors.accent.onAccent,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = name,
                    style = Theme.typography.caption,
                    color = Theme.colors.label.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (category != null) {
                    TagChip(text = category)
                }
            }
        }
    }
}

@Preview
@Composable
private fun ItemCardPreview() {
    ItemCard(
        imageUrl = "",
        name = "Белая футболка",
        category = "Верх",
        onClick = {},
    )
}

@Preview
@Composable
private fun ItemCardSelectedPreview() {
    ItemCard(
        imageUrl = "",
        name = "Чёрные джинсы",
        category = "Низ",
        onClick = {},
        selectionMode = true,
        isSelected = true,
    )
}
