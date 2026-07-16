package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.designsystem.components.SkeletonCard
import com.ownstd.project.designsystem.components.rememberShimmerTranslation
import com.ownstd.project.pincard.internal.external.rememberShareManager
import com.ownstd.project.pincard.internal.presentation.viewmodel.GenerateLooksError
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Looks(
    viewModel: LooksViewModel,
    onNavigateToConstructor: () -> Unit,
    navigateToDetails: (lookId: Int) -> Unit,
) {
    val looksList by viewModel.looks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generateError by viewModel.generateError.collectAsState()
    val shareManager = rememberShareManager()
    val snackbarHostState = remember { SnackbarHostState() }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = { onNavigateToConstructor() }
    )

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
            .pullRefresh(pullRefreshState)
    ) {
        when {
            isLoading && looksList.isEmpty() -> {
                SkeletonLooksGrid()
            }
            looksList.isEmpty() && !isGenerating -> {
                OutfitsEmptyState(
                    onBuildOutfitClick = onNavigateToConstructor,
                    onGenerateOutfitsClick = { if (!isGenerating) viewModel.generateLooks() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = navBarBottom + 76.dp),
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
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 76.dp + 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
        }

        PullRefreshIndicator(
            refreshing = false,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 76.dp)
        )
    }
}

@Composable
private fun SkeletonLooksGrid() {
    val shimmer by rememberShimmerTranslation()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = navBarBottom + 76.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(6) {
            SkeletonCard(
                shimmerTranslation = shimmer,
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )
        }
    }
}
