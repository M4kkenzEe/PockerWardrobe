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
import com.ownstd.project.internal.compose.OutfitCard
import com.ownstd.project.internal.compose.Stories
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.Res
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.clothe1
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.clothe2
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.clothe3
import kotlinprojecttesting.features.recommendations_page_screen.generated.resources.clothe4

@Composable
fun RecommendationsPageScreen() {

    val outfitsList = listOf(
        Res.drawable.clothe1,
        Res.drawable.clothe2,
        Res.drawable.clothe3,
        Res.drawable.clothe4,
        Res.drawable.clothe3,
        Res.drawable.clothe4,
        Res.drawable.clothe3,
        Res.drawable.clothe4,
        Res.drawable.clothe1,
        Res.drawable.clothe2,
        Res.drawable.clothe3,
        Res.drawable.clothe1,
        Res.drawable.clothe2,
        Res.drawable.clothe3,
        Res.drawable.clothe1,
        Res.drawable.clothe1,
        Res.drawable.clothe2,
        Res.drawable.clothe1,
        Res.drawable.clothe3,
        Res.drawable.clothe1,
        Res.drawable.clothe3,
        Res.drawable.clothe1,
        Res.drawable.clothe1,
        Res.drawable.clothe2,
        Res.drawable.clothe1,
        Res.drawable.clothe3,
        Res.drawable.clothe1,
        Res.drawable.clothe2,
        Res.drawable.clothe3,
    )

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
        items(outfitsList.size) {
            OutfitCard(img = outfitsList[it])
        }
    }
}

