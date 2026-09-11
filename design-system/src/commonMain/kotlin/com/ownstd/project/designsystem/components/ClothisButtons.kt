package com.ownstd.project.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ownstd.project.designsystem.theme.ClothisTheme

@Composable
fun ClothisPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.heightIn(min = dimens.buttonHeight),
        shape = RoundedCornerShape(dimens.radiusButton),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colors.clay,
            contentColor = colors.onClay,
            disabledBackgroundColor = colors.surfaceElevated,
            disabledContentColor = colors.onCanvasMuted,
        ),
        contentPadding = PaddingValues(horizontal = dimens.spaceLg, vertical = dimens.spaceMd),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm, Alignment.CenterHorizontally),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimens.iconButtonIcon),
                    color = colors.onCanvasMuted,
                    strokeWidth = dimens.progressStroke,
                )
            }
            Text(
                text = text,
                style = ClothisTheme.typography.title,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

@Composable
fun ClothisGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = dimens.buttonHeight),
        shape = RoundedCornerShape(dimens.radiusButton),
        elevation = null,
        border = BorderStroke(dimens.borderWidth, colors.divider),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colors.surfaceElevated,
            contentColor = colors.onCanvas,
            disabledBackgroundColor = colors.surfaceElevated,
            disabledContentColor = colors.onCanvasMuted,
        ),
        contentPadding = PaddingValues(horizontal = dimens.spaceLg, vertical = dimens.spaceMd),
    ) {
        Text(text = text, style = ClothisTheme.typography.title, textAlign = TextAlign.Center)
    }
}
