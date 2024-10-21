package com.ownstd.project.internal.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OutfitsGrid() {
    val items = List(19) { "Item ${it + 1}" }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Проходим по списку, группируя по 2 элемента
        for (rowItems in items.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(), // Занимаем всю ширину строки
                horizontalArrangement = if (rowItems.size == 1) Arrangement.Center else Arrangement.SpaceEvenly // Центрируем если 1 элемент, или распределяем если 2
            ) {
                rowItems.forEach { item ->
                    OutfitCard(text = item, modifier = Modifier.weight(1f))
                }
                // Если элементов меньше 2, заполняем оставшееся место Spacer-ом
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun OutfitCard(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(200.dp)
            .clip(RoundedCornerShape(20))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}