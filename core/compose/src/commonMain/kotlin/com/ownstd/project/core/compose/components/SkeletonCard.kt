package com.ownstd.project.core.compose.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import org.jetbrains.compose.ui.tooling.preview.Preview

private val CORNER_RADIUS = 16.dp

@Composable
fun SkeletonCard(
    aspectRatio: Float = 1f,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(Theme.colors.background.surface)
            .alpha(alpha),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .background(Theme.colors.component.skeleton),
        )
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Theme.colors.component.skeleton),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Theme.colors.component.skeleton),
            )
        }
    }
}

@Preview
@Composable
private fun SkeletonCardPreview() {
    SkeletonCard()
}
