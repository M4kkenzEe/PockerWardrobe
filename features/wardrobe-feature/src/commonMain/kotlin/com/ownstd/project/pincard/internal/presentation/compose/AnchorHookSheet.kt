package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ownstd.project.designsystem.components.ClothisGhostButton
import com.ownstd.project.designsystem.components.ClothisPrimaryButton
import com.ownstd.project.designsystem.theme.ClothisTheme
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import com.ownstd.project.pincard.internal.presentation.viewmodel.AnchorHookState
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.Res
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.anchor_hook_cta
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.anchor_hook_dismiss
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.anchor_hook_generating
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.anchor_hook_title
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.wear_with_all_saved
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.wear_with_need_more
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Swipe-dismiss threshold in density-independent pixels
private const val SWIPE_DOWN_THRESHOLD_DP = 80

@Composable
internal fun AnchorHookSheet(
    state: AnchorHookState,
    onGenerateDrafts: (clotheId: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVisible = state !is AnchorHookState.Hidden && state !is AnchorHookState.Error

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing),
        ) + fadeIn(animationSpec = tween(350)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250),
        ) + fadeOut(animationSpec = tween(250)),
    ) {
        AnchorHookSheetContent(
            state = state,
            onGenerateDrafts = onGenerateDrafts,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun AnchorHookSheetContent(
    state: AnchorHookState,
    onGenerateDrafts: (clotheId: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var dragAccumulator by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = dimens.radiusSheet, topEnd = dimens.radiusSheet))
            .background(colors.surfaceElevated)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = {
                        if (dragAccumulator > SWIPE_DOWN_THRESHOLD_DP * density) onDismiss()
                        dragAccumulator = 0f
                    },
                    onDragCancel = { dragAccumulator = 0f },
                    onVerticalDrag = { _, delta ->
                        if (delta > 0) dragAccumulator += delta
                    },
                )
            }
            .padding(
                start = dimens.screenHorizontalPadding,
                end = dimens.screenHorizontalPadding,
                top = dimens.spaceLg,
                // Raise content above the nav island so "Позже" is always tappable
                bottom = navBarBottom + dimens.navIslandHeight + dimens.navIslandBottomGap,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.spaceMd),
        ) {
            DragHandle(colors.onCanvasMuted)
            Spacer(modifier = Modifier.height(dimens.spaceXs))
            if (state is AnchorHookState.Celebrate) {
                CelebrateBurst(imageUrl = state.imageUrl)
                Spacer(modifier = Modifier.height(dimens.spaceSm))
            }
            SheetTitle(state)
            Spacer(modifier = Modifier.height(dimens.spaceSm))
            SheetActions(
                state = state,
                onGenerateDrafts = onGenerateDrafts,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun DragHandle(color: Color) {
    val dimens = ClothisTheme.dimens
    Box(
        modifier = Modifier
            .width(dimens.space3xl)
            .height(dimens.spaceXs)
            .clip(RoundedCornerShape(dimens.progressStroke))
            .background(color.copy(alpha = 0.4f))
    )
}

@Composable
private fun CelebrateBurst(imageUrl: String?) {
    val burstProgress = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
        burstProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing),
        )
    }

    val burstColor = ClothisTheme.colors.clay
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(88.dp),
    ) {
        Canvas(modifier = Modifier.size(88.dp)) {
            drawBurst(burstProgress.value, burstColor)
        }
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .scale(iconScale.value),
        )
    }
}

private fun DrawScope.drawBurst(progress: Float, color: Color) {
    if (progress <= 0f) return
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = size.minDimension / 2
    val particleCount = 8
    val particleRadius = 4.dp.toPx()

    repeat(particleCount) { index ->
        val angle = (2 * PI * index / particleCount).toFloat()
        val distance = maxRadius * progress
        val x = centerX + cos(angle) * distance
        val y = centerY + sin(angle) * distance
        val alpha = (1f - progress).coerceIn(0f, 1f)
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = particleRadius * (1f - progress * 0.5f),
            center = Offset(x, y),
        )
    }
}

@Composable
private fun SheetTitle(state: AnchorHookState) {
    val colors = ClothisTheme.colors
    val text = when (state) {
        is AnchorHookState.Celebrate, is AnchorHookState.Generating ->
            stringResource(Res.string.anchor_hook_title)
        is AnchorHookState.NeedMore -> {
            val labels = state.missing.map { stringResource(missingCategoryResource(it)) }.joinToString(", ")
            stringResource(Res.string.wear_with_need_more, labels)
        }
        AnchorHookState.AllSaved -> stringResource(Res.string.wear_with_all_saved)
        AnchorHookState.Hidden -> ""
        AnchorHookState.Error -> ""
    }
    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = ClothisTheme.typography.title,
            color = colors.onCanvas,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SheetActions(
    state: AnchorHookState,
    onGenerateDrafts: (clotheId: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val isGenerating = state is AnchorHookState.Generating
    val clotheId = when (state) {
        is AnchorHookState.Celebrate -> state.clotheId
        is AnchorHookState.Generating -> state.clotheId
        is AnchorHookState.NeedMore -> null
        AnchorHookState.AllSaved -> null
        AnchorHookState.Hidden -> null
        AnchorHookState.Error -> null
    }
    val showCta = state is AnchorHookState.Celebrate || state is AnchorHookState.Generating

    if (showCta && clotheId != null) {
        ClothisPrimaryButton(
            text = if (isGenerating) stringResource(Res.string.anchor_hook_generating)
                   else stringResource(Res.string.anchor_hook_cta),
            onClick = { onGenerateDrafts(clotheId) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = isGenerating,
        )
    }

    if (state is AnchorHookState.NeedMore || state is AnchorHookState.AllSaved || showCta) {
        ClothisGhostButton(
            text = stringResource(Res.string.anchor_hook_dismiss),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ---- Previews ----

@Preview
@Composable
private fun PreviewCelebrate() {
    ClothisTheme {
        AnchorHookSheet(
            state = AnchorHookState.Celebrate(clotheId = 1, imageUrl = null),
            onGenerateDrafts = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun PreviewGenerating() {
    ClothisTheme {
        AnchorHookSheet(
            state = AnchorHookState.Generating(clotheId = 1),
            onGenerateDrafts = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNeedMore() {
    ClothisTheme {
        AnchorHookSheet(
            state = AnchorHookState.NeedMore(
                missing = listOf(MissingCategory.Bottom, MissingCategory.Shoes)
            ),
            onGenerateDrafts = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAllSaved() {
    ClothisTheme {
        AnchorHookSheet(
            state = AnchorHookState.AllSaved,
            onGenerateDrafts = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun PreviewHidden() {
    ClothisTheme {
        AnchorHookSheet(
            state = AnchorHookState.Hidden,
            onGenerateDrafts = {},
            onDismiss = {},
        )
    }
}
