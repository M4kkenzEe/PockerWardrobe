package com.ownstd.project.api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize 
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.internal.compose.CategoriesCards
import com.ownstd.project.internal.compose.Stories

@Composable
fun RecommendationsPageScreen() {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Stories()
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            CategoriesCards()
        }
        /*items(outfitsList.size) {
            OutfitCard(img = outfitsList[it])
        }*/
    }
}

