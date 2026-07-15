package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import com.ownstd.project.pincard.internal.replaceFragment
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Wardrobe(viewModel: WardrobeViewModel, onClotheClick: (Int) -> Unit = {}) {
    val clothes by viewModel.clothes.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()
    val showPaywall by viewModel.showPaywall.collectAsState()
    val selectedOccasion by viewModel.selectedOccasionFilter.collectAsState()

    var requestAddClothe by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isUploading,
        onRefresh = { if (!isUploading) requestAddClothe = true }
    )
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uploadError) {
        if (uploadError) {
            snackbarHostState.showSnackbar("Не удалось добавить вещь. Попробуйте ещё раз")
            viewModel.clearUploadError()
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetBackgroundColor = Color(0xFF1E1E1E),
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            OccasionPickerSheetContent(
                onOccasionSelected = { occasion ->
                    coroutineScope.launch { sheetState.hide() }
                    pendingBitmap?.let { viewModel.loadClothe(it, occasion) }
                    pendingBitmap = null
                },
                onSkip = {
                    coroutineScope.launch { sheetState.hide() }
                    pendingBitmap?.let { viewModel.loadClothe(it, null) }
                    pendingBitmap = null
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            if (clothes.isEmpty() && !isUploading && selectedOccasion == null) {
                WardrobeEmptyState(
                    onAddClick = { requestAddClothe = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            bottom = navBarBottom + 76.dp, // nav bar + island(64) + gap(12)
                        ),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(clothes, key = { it.id!! }) { clothe ->
                            ClotheCard(
                                clotheUrl = clothe.imageUrl.replaceFragment(),
                                onClick = { onClotheClick(clothe.id!!) },
                                onDelete = { viewModel.deleteClothe(clothe.id!!) },
                                modifier = Modifier.animateItem()
                            )
                        }

                        if (isUploading) {
                            item { SkeletonClotheCard() }
                        }
                    }
                }
            }

            AddClotheFloatButton(
                onButtonClick = { bitmap ->
                    pendingBitmap = bitmap
                    coroutineScope.launch { sheetState.show() }
                },
                requestLaunch = requestAddClothe,
                onLaunchConsumed = { requestAddClothe = false }
            )

            PullRefreshIndicator(
                refreshing = isUploading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 76.dp)
            )

            if (showPaywall) {
                PaywallScreen(onDismiss = { viewModel.dismissPaywall() })
            }
        }
    }
}

@Composable
private fun SkeletonClotheCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20))
            .background(Color(0xFF3A3A3A))
    )
}

@Composable
private fun WardrobeEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "👗", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Гардероб пуст",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Сфотографируйте вещь или загрузите из галереи",
            fontSize = 14.sp,
            color = Color(0xFF9AA0A6),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8AB4F8))
        ) {
            Text(
                text = "Добавить вещь",
                color = Color(0xFF27272B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
