package com.ownstd.project.internal.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.Res
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.beefre
import org.jetbrains.compose.resources.painterResource

@Composable
fun Stories() {
    LazyRow(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(9) { StoryCard() }
    }
}

@Composable
fun StoryCard() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.Cyan)
            .border(BorderStroke(2.dp, Color.White), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.beefre),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
    }
}