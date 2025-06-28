package com.ownstd.project.wardrobe.api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.wardrobe.internal.OutfitCard
import com.ownstd.project.wardrobe.resources.Res
import com.ownstd.project.wardrobe.resources.clothe1
import com.ownstd.project.wardrobe.resources.clothe2
import com.ownstd.project.wardrobe.resources.clothe3
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WardrobeScreen() {
    val outfitsList = mutableListOf(
        Res.drawable.clothe1,
        Res.drawable.clothe1,
        Res.drawable.clothe2,
        Res.drawable.clothe3,
        Res.drawable.clothe3,
        Res.drawable.clothe1,
        Res.drawable.clothe3,
        Res.drawable.clothe2,
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

@Preview
@Composable
fun WardrobeScreenPreview() {
    WardrobeScreen()
}