package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.pincard.internal.external.rememberShareManager
import com.ownstd.project.pincard.internal.presentation.viewmodel.GenerateLooksError
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel

@Composable
internal fun Looks(
    viewModel: LooksViewModel,
    onNavigateToConstructor: () -> Unit,
    navigateToDetails: (lookId: Int) -> Unit,
    onNavigateToTinderOutfit: () -> Unit = {}
) {
    val looksList by viewModel.looks.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generateError by viewModel.generateError.collectAsState()
    val shareManager = rememberShareManager()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(generateError) {
        when (generateError) {
            is GenerateLooksError.NotEnoughClothes -> {
                snackbarHostState.showSnackbar("Добавьте минимум 3 вещи для генерации образа")
                viewModel.clearGenerateError()
            }
            is GenerateLooksError.NetworkError -> {
                snackbarHostState.showSnackbar("Не удалось сгенерировать образы. Попробуйте позже")
                viewModel.clearGenerateError()
            }
            null -> {}
        }
    }

    DisposableEffect(Unit) {
        viewModel.getLooks()
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .padding(top = 12.dp)
    ) {
        if (looksList.isEmpty() && !isGenerating) {
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
                items(looksList, key = { it.id!! }) { look ->
                    LookCard(
                        lookUrl = look.url,
                        onClick = { navigateToDetails(look.id ?: 0) },
                        onDelete = { viewModel.deleteLook(look.id!!) },
                        onShare = {
                            viewModel.shareLook(look.id!!) { shareUrl ->
                                shareManager.shareText(shareUrl, "Поделиться образом")
                            }
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = onNavigateToTinderOutfit,
                backgroundColor = Color(0xFF4CAF50)
            ) {
                Text("👗", fontSize = 22.sp)
            }
            FloatingActionButton(
                onClick = { if (!isGenerating) viewModel.generateLooks() },
                backgroundColor = Color(0xFFBB86FC)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("✨", fontSize = 22.sp)
                }
            }
            FloatingActionButton(
                onClick = { if (!isGenerating) onNavigateToConstructor() },
                modifier = Modifier.alpha(if (isGenerating) 0.5f else 1f)
            ) {
                Text("+", fontSize = 32.sp)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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

