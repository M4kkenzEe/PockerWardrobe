package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ownstd.project.designsystem.components.SkeletonCard
import com.ownstd.project.designsystem.components.rememberShimmerTranslation
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.presentation.viewmodel.TinderOutfitState
import com.ownstd.project.pincard.internal.presentation.viewmodel.TinderOutfitViewModel
import com.ownstd.project.pincard.internal.replaceFragment
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TinderOutfitScreen(
    onBackClick: () -> Unit = {}
) {
    val viewModel: TinderOutfitViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    TinderOutfitContent(
        state = state,
        onBackClick = onBackClick,
        onSkip = { lookId -> viewModel.onSkip(lookId) },
        onLike = { lookId -> viewModel.onLike(lookId) }
    )
}

@Composable
private fun TinderOutfitContent(
    state: TinderOutfitState,
    onBackClick: () -> Unit,
    onSkip: (Int) -> Unit,
    onLike: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp)
    ) {
        TinderTopBar(onBackClick = onBackClick)

        when {
            state.isLoading -> {
                TinderLoadingSkeleton(modifier = Modifier.weight(1f))
            }
            state.isEmpty -> {
                TinderEmptyState(modifier = Modifier.fillMaxSize())
            }
            else -> {
                TinderDeck(
                    state = state,
                    onSkip = onSkip,
                    onLike = onLike,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TinderTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
        ) {
            Text("←", fontSize = 18.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Что надеть сегодня?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun TinderDeck(
    state: TinderOutfitState,
    onSkip: (Int) -> Unit,
    onLike: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val current = state.current ?: return

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
        ) { look ->
            LookBigCard(look = look)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.upcoming.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.upcoming.forEach { previewLook ->
                    LookSmallCard(
                        look = previewLook,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (state.upcoming.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                label = "✕",
                backgroundColor = Color(0xFF3A1A1A),
                contentColor = Color(0xFFFF5252),
                size = 64,
                fontSize = 28,
                onClick = { current.id?.let { onSkip(it) } }
            )
            ActionButton(
                label = "♥",
                backgroundColor = Color(0xFF1A3A1A),
                contentColor = Color(0xFF69F0AE),
                size = 72,
                fontSize = 32,
                onClick = { current.id?.let { onLike(it) } }
            )
        }
    }
}

@Composable
private fun LookBigCard(look: Look, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E1E1E)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = look.url.replaceFragment(),
            contentDescription = look.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        if (look.name.isNotEmpty()) {
            Text(
                text = look.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun LookSmallCard(look: Look, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = look.url.replaceFragment(),
            contentDescription = look.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    size: Int,
    fontSize: Int,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size.dp)
            .background(backgroundColor, CircleShape)
    ) {
        Text(label, fontSize = fontSize.sp, color = contentColor)
    }
}

@Composable
private fun TinderLoadingSkeleton(modifier: Modifier = Modifier) {
    val shimmer by rememberShimmerTranslation()
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        SkeletonCard(
            shimmerTranslation = shimmer,
            modifier = Modifier.fillMaxWidth().weight(0.65f),
            shape = RoundedCornerShape(20.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonCard(shimmerTranslation = shimmer, modifier = Modifier.size(64.dp), shape = CircleShape)
            SkeletonCard(shimmerTranslation = shimmer, modifier = Modifier.size(72.dp), shape = CircleShape)
        }
    }
}

@Composable
private fun TinderEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👗", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "На сегодня всё",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Загляни завтра или добавь больше вещей в гардероб",
            fontSize = 14.sp,
            color = Color(0xFF9AA0A6),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}
