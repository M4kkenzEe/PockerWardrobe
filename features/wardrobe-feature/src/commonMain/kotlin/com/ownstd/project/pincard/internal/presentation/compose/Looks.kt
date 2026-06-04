package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel
import com.ownstd.project.storage.getClipboardManager

@Composable
internal fun Looks(
    viewModel: LooksViewModel,
    onNavigateToConstructor: () -> Unit,
    navigateToDetails: (lookId: Int) -> Unit
) {
    val looksList by viewModel.looks.collectAsState()
    val clipboardManager = rememberClipboardManager()

    DisposableEffect(Unit) {
        viewModel.getLooks()
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 44.dp)
            .padding(horizontal = 14.dp)
            .padding(top = 12.dp)
    ) {
        if (looksList.isEmpty()) {
            LooksEmptyState(
                onCreateClick = onNavigateToConstructor,
                modifier = Modifier.fillMaxSize()
            )
        } else {
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
                        onDelete = { viewModel.deleteLook(look.id!!) },
                        onShare = {
                            viewModel.shareLook(look.id!!) { shareUrl ->
                                clipboardManager.copyToClipboard(shareUrl)
                            }
                        }
                    )
                }
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

@Composable
private fun LooksEmptyState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "✨", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Образов ещё нет",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Создайте образ из ваших вещей",
            fontSize = 14.sp,
            color = Color(0xFF9AA0A6),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8AB4F8))
        ) {
            Text(
                text = "Создать образ",
                color = Color(0xFF27272B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
expect fun rememberClipboardManager(): com.ownstd.project.storage.ClipboardManager