package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel

@Composable
internal fun Looks(
    viewModel: LooksViewModel,
    onNavigateToConstructor: () -> Unit,
    navigateToDetails: (lookId: Int) -> Unit
) {

    val looksList by viewModel.looks.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 44.dp)
            .padding(horizontal = 14.dp)
            .padding(top = 12.dp)
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(looksList) { look ->
                LookCard(
                    lookUrl = look.url,
                    onClick = { navigateToDetails(look.id ?: 0) },
                    onDelete = { viewModel.deleteLook(look.id!!) }
                )
            }
        }
        FloatingActionButton(
            onClick = {
                onNavigateToConstructor()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text("+", fontSize = 32.sp)
        }
    }
}