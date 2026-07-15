package com.ownstd.project.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.ownstd.project.designsystem.theme.ClothisTheme

@Composable
fun ClothisIconButton(
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens

    Box(
        modifier = modifier
            .size(dimens.iconButtonSize)
            .clip(CircleShape)
            .background(colors.surfaceElevated)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(dimens.iconButtonIcon),
            colorFilter = ColorFilter.tint(colors.onCanvas),
        )
    }
}
