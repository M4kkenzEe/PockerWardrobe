package com.ownstd.project.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

// Shimmer tokens — dark warm palette matching canvas (#141210)
// base = surfaceElevated (#26231F), highlight = OnCanvasMuted @9% alpha over base
private val SkeletonBase = Color(0xFF26231F)
private val SkeletonHighlight = Color(0xFF322E2A)

/**
 * Returns an animated fraction [−1, 2] driving the shimmer sweep.
 * Call once per parent container and pass [shimmerTranslation] down to each [SkeletonCard]
 * so all cards share a single synchronized animation (1200 ms, linear, restart).
 */
@Composable
fun rememberShimmerTranslation(): State<Float> {
    val transition = rememberInfiniteTransition(label = "shimmer")
    return transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
}

/**
 * Rounded skeleton placeholder with a left-to-right shimmer sweep.
 *
 * Pass [shimmerTranslation] from [rememberShimmerTranslation] called at the parent level
 * so multiple cards animate in sync. Size is controlled entirely via [modifier].
 * Use [shape] to match the actual content shape (default is 20% rounded, same as wardrobe cards).
 */
@Composable
fun SkeletonCard(
    shimmerTranslation: Float,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                val w = size.width
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(SkeletonBase, SkeletonHighlight, SkeletonBase),
                        start = Offset(shimmerTranslation * w, 0f),
                        end = Offset((shimmerTranslation + 1f) * w, 0f),
                    )
                )
            }
    )
}

@Preview
@Composable
fun SkeletonCardPreview() {
    val heights = listOf(180.dp, 240.dp, 200.dp, 160.dp, 220.dp, 190.dp)
    val shimmer by rememberShimmerTranslation()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141210))
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
        ) {
            items(heights) { h: Dp ->
                SkeletonCard(
                    shimmerTranslation = shimmer,
                    modifier = Modifier.height(h),
                )
            }
        }
    }
}
