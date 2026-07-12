package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import com.ownstd.project.designsystem.components.ClothisGhostButton
import com.ownstd.project.designsystem.components.ClothisPrimaryButton
import com.ownstd.project.designsystem.theme.ClothisTheme
import kotlinprojecttesting.design_system.generated.resources.Res
import kotlinprojecttesting.design_system.generated.resources.ic_checkroom
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun OutfitsEmptyState(
    onBuildOutfitClick: () -> Unit,
    onGenerateOutfitsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ClothisTheme.colors
    val typography = ClothisTheme.typography
    val dimens = ClothisTheme.dimens

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = dimens.emptyStateMaxWidth)
                .padding(horizontal = dimens.screenHorizontalPadding),
        ) {
            // Icon tile — 128×128 surfaceElevated with hanger icon tinted clay
            Box(
                modifier = Modifier
                    .size(dimens.emptyStateTileSize)
                    .clip(RoundedCornerShape(dimens.radiusTile))
                    .background(colors.surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_checkroom),
                    contentDescription = null,
                    modifier = Modifier.size(dimens.emptyStateTileIconSize),
                    colorFilter = ColorFilter.tint(colors.clay),
                )
            }

            Spacer(modifier = Modifier.height(dimens.spaceMd))

            Text(
                text = "Образов пока нет",
                style = typography.h1,
                color = colors.onCanvas,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(dimens.spaceSm))

            Text(
                text = "Собери образ вручную или дай приложению собрать за тебя",
                style = typography.body,
                color = colors.onCanvasMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(dimens.space4xl))

            ClothisPrimaryButton(
                text = "Собрать образ",
                onClick = onBuildOutfitClick,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(dimens.spaceMd))

            ClothisGhostButton(
                text = "✨ Сгенерировать образы",
                onClick = onGenerateOutfitsClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
