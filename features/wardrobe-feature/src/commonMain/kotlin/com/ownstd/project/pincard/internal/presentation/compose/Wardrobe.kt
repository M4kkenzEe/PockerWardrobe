package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import androidx.compose.ui.window.Dialog
import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import com.ownstd.project.pincard.internal.replaceFragment
import kotlinx.coroutines.launch

@Composable
internal fun Wardrobe(viewModel: WardrobeViewModel) {
    val clothes by viewModel.clothes.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()

    var dialogState by remember { mutableStateOf(false) }
    var urlState by remember { mutableStateOf("") }
    var requestAddClothe by remember { mutableStateOf(false) }
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
                .padding(bottom = 44.dp)
        ) {
            if (clothes.isEmpty() && !isUploading) {
                WardrobeEmptyState(
                    onAddClick = { requestAddClothe = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(clothes, key = { it.id!! }) { clothe ->
                        ClotheCard(
                            clotheUrl = clothe.imageUrl.replaceFragment(),
                            onDelete = { viewModel.deleteClothe(clothe.id!!) },
                            modifier = Modifier.animateItem()
                        )
                    }

                    if (isUploading) {
                        item { SkeletonClotheCard() }
                    }

                    val totalItems = clothes.size + if (isUploading) 1 else 0
                    val count = if (totalItems % 2 == 0) 2 else 3
                    items(count) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
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
                    onClick = { dialogState = true },
                    modifier = Modifier
                ) {
                    Text("WB", fontSize = 32.sp)
                }
                if (isUploading) {
                    FloatingActionButton(onClick = {}) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    AddClotheFloatButton(
                        modifier = Modifier,
                        onButtonClick = { bitmap ->
                            pendingBitmap = bitmap
                            coroutineScope.launch { sheetState.show() }
                        },
                        requestLaunch = requestAddClothe,
                        onLaunchConsumed = { requestAddClothe = false }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (dialogState) {
                Dialog(
                    onDismissRequest = { dialogState = false },
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.background(Color.White)
                    ) {
                        TextField(onValueChange = { urlState = it }, value = urlState)
                        Button(onClick = {
                            viewModel.uploadFromUrl(urlState)
                            urlState = ""
                            dialogState = false
                        }) {
                            Text("Import")
                        }
                    }
                }
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
