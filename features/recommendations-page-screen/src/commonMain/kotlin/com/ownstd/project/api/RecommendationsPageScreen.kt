package com.ownstd.project.api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.internal.compose.CategoriesCard
import com.ownstd.project.internal.compose.OutfitsGrid
import com.ownstd.project.internal.compose.Stories

@Composable
fun RecommendationsPageScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Stories() }
        item { CategoriesCard() }
        item { OutfitsGrid() }
    }
}

