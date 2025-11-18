package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.ownstd.project.pincard.internal.presentation.model.LookItemUiState
import com.ownstd.project.pincard.internal.presentation.viewmodel.ConstructorViewModel
import com.ownstd.project.pincard.internal.replaceFragment
import io.github.suwasto.capturablecompose.Capturable
import io.github.suwasto.capturablecompose.CompressionFormat
import io.github.suwasto.capturablecompose.rememberCaptureController
import io.github.suwasto.capturablecompose.toByteArray
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

// Design System Colors
private val BG_GREY_COLOR = Color(0xFF27272B)
private val BLUE_COLOR = Color(0xFF8AB4F8)
private val GREY_COLOR = Color(0xFF9AA0A6)
private val CAPTURE_ZONE_BG = Color(0xFFF5F5F5)

@Composable
fun LookConstructor(
    backClick: () -> Unit = {},
    navigateToSavedLooks: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<ConstructorViewModel>()

    val clotheList by viewModel.clotheList.collectAsState()
    val pickedClothes by viewModel.pickedClotheList.collectAsState()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val captureController = rememberCaptureController()

    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Handle back click - show dialog if there are unsaved items
    val handleBackClick = {
        if (pickedClothes.isNotEmpty()) {
            showUnsavedDialog = true
        } else {
            backClick()
        }
    }

    // Alert dialog for unsaved items
    if (showUnsavedDialog) {
        ConstructorAlertDialog(
            title = "Несохранённые изменения",
            message = "Вы уверены что хотите вернуться, сохраненный образ удалится?",
            confirmText = "Выйти",
            cancelText = "Отмена",
            onConfirm = {
                showUnsavedDialog = false
                backClick()
            },
            onCancel = {
                showUnsavedDialog = false
            }
        )
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetBackgroundColor = Color.White,
        sheetContent = {
            BottomSheetContent(
                clotheList = clotheList,
                onClotheClick = { clothe ->
                    viewModel.pickClothe(clothe)
                    scope.launch { sheetState.hide() }
                },
                onDismiss = { scope.launch { sheetState.hide() } }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(BG_GREY_COLOR),
            backgroundColor = BG_GREY_COLOR,
            topBar = {
                ConstructorTopBar(
                    onBackClick = handleBackClick,
                    onAddClick = { scope.launch { sheetState.show() } }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // White Card Container with Screenshot Zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    // Screenshot Capture Zone
                    Capturable(
                        captureController = captureController,
                        onCaptured = { imageBitmap ->
                            val byteArray = imageBitmap.toByteArray(CompressionFormat.PNG, 100)
                            viewModel.save(byteArray, onSuccess = {
                                navigateToSavedLooks()
                            })
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
//                                .background(CAPTURE_ZONE_BG)
                        ) {
                            // Render picked clothes
                            for (clothe in pickedClothes) {
                                ClotheItem(
                                    item = clothe,
                                    onUpdate = { transform -> viewModel.update(clothe.id, transform) },
                                    bringToFront = viewModel::bringToFront,
                                    onClick = { viewModel.setLastInteracted(clothe.id) }
                                )
                            }
                        }
                    }

                    // Delete FAB (floating inside white card - left)
                    FloatingActionButton(
                        onClick = { viewModel.deleteLastInteracted() },
                        backgroundColor = Color.White,
                        contentColor = Color.Red,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .size(56.dp)
                    ) {
                        Text(
                            text = "🗑",
                            fontSize = 24.sp
                        )
                    }

                    // Save FAB (floating inside white card - right)
                    FloatingActionButton(
                        onClick = { captureController.capture() },
                        backgroundColor = BLUE_COLOR,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(56.dp)
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }
}

@Composable
internal fun ClotheItem(
    item: LookItemUiState,
    onUpdate: ((LookItemUiState) -> LookItemUiState) -> Unit,
    bringToFront: (Int) -> Unit,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(item.offsetX.roundToInt(), item.offsetY.roundToInt()) }
            .wrapContentSize()
            .zIndex(item.zIndex)
            .clickable { onClick() }
            .pointerInput(item.id) {
                detectTransformGestures { _, pan, zoomChange, rotationChange ->
                    onUpdate { state ->
                        state.copy(
                            offsetX = state.offsetX + pan.x,
                            offsetY = state.offsetY + pan.y,
                            size = maxOf(20.dp, state.size * zoomChange),
                            rotation = state.rotation + rotationChange
                        )
                    }
                    bringToFront(item.id)
                }
            }
    ) {
        AsyncImage(
            model = item.imgUrl.replaceFragment(),
            modifier = Modifier
                .size(item.size)
                .graphicsLayer(rotationZ = item.rotation),
            contentDescription = "",
        )
    }
}

//TODO вынести в дизайн систему
@Composable
private fun ConstructorAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BG_GREY_COLOR)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = GREY_COLOR,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = cancelText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Confirm Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = BLUE_COLOR,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = confirmText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ConstructorTopBar(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BG_GREY_COLOR)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(onClick = onBackClick) {
            Text(
                text = "←",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Title
        Text(
            text = "Конструктор образов",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )

        // Add Item Button
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = BLUE_COLOR,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = "+ Вещь",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BottomSheetContent(
    clotheList: List<com.ownstd.project.pincard.internal.data.model.Clothe>,
    onClotheClick: (com.ownstd.project.pincard.internal.data.model.Clothe) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Выберите вещь",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            IconButton(onClick = onDismiss) {
                Text(
                    text = "✕",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GREY_COLOR
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of clothes
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(clotheList) { clothe ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                        .clickable { onClotheClick(clothe) }
                ) {
                    AsyncImage(
                        model = clothe.imageUrl?.replaceFragment(),
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
