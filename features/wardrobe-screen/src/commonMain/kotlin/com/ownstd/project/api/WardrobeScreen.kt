package com.ownstd.project.api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.internal.OutfitCard
import com.ownstd.project.wardrobe.resources.Res
import com.ownstd.project.wardrobe.resources.c1
import com.ownstd.project.wardrobe.resources.c2
import com.ownstd.project.wardrobe.resources.c3
import com.ownstd.project.wardrobe.resources.c4

@Composable
fun WardrobeScreen() {
    val outfitsList = listOf(
        Res.drawable.c1,
        Res.drawable.c2,
        Res.drawable.c3,
        Res.drawable.c4,
        Res.drawable.c3,
        Res.drawable.c4,
        Res.drawable.c3,
        Res.drawable.c4,
        Res.drawable.c1,
        Res.drawable.c2,
        Res.drawable.c3,
        Res.drawable.c1,
        Res.drawable.c2,
        Res.drawable.c3,
        Res.drawable.c1,
        Res.drawable.c1,
        Res.drawable.c2,
        Res.drawable.c1,
        Res.drawable.c3,
        Res.drawable.c1,
        Res.drawable.c3,
        Res.drawable.c1,
        Res.drawable.c1,
        Res.drawable.c2,
        Res.drawable.c1,
        Res.drawable.c3,
        Res.drawable.c1,
        Res.drawable.c2,
        Res.drawable.c3,
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(outfitsList.size) {
            OutfitCard(img = outfitsList[it])
        }
    }
}