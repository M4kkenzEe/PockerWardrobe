package com.ownstd.project.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun OutfitCard(modifier: Modifier = Modifier, text: String = "", img: DrawableResource) {
    Box(
        modifier = modifier
            .width(160.dp)
            .height(250.dp)
            .clip(RoundedCornerShape(20))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(img),
            contentDescription = "",
            modifier = Modifier.padding(vertical = 100.dp)
        )
    }
}