package com.ownstd.project.internal.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.Res
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.categorie_1
import org.jetbrains.compose.resources.painterResource

@Composable
fun CategoriesCards() {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(6) { CategoryCard() }
    }
}

@Composable
fun CategoryCard() {
    Box(
        modifier = Modifier
            .size(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Cyan),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.categorie_1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.6f))
        )
        Text(
            modifier = Modifier,
            text = "Goblin Core",
            color = Color.White,
            fontWeight = FontWeight(400),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
    }
}