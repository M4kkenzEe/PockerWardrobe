package com.ownstd.project.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ownstd.project.designsystem.theme.ClothisTheme

@Composable
fun ClothisPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ClothisTheme.colors
    val typography = ClothisTheme.typography
    val dimens = ClothisTheme.dimens
    val shape = RoundedCornerShape(dimens.radiusButton)

    Box(
        modifier = modifier
            .height(dimens.buttonHeight)
            .shadow(elevation = 4.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.2f))
            .clip(shape)
            .background(colors.clay)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = typography.title, color = colors.onClay)
    }
}

@Composable
fun ClothisGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ClothisTheme.colors
    val typography = ClothisTheme.typography
    val dimens = ClothisTheme.dimens
    val shape = RoundedCornerShape(dimens.radiusButton)
    val ghostColor = colors.ghostSurface.copy(alpha = 0.10f)

    Box(
        modifier = modifier
            .height(dimens.buttonHeight)
            .clip(shape)
            .background(ghostColor)
            .border(width = 1.dp, color = ghostColor, shape = shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = typography.title, color = colors.clay)
    }
}
