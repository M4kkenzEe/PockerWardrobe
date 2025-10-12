package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.parseToHost
import com.ownstd.project.pincard.internal.presentation.model.LookItemUiState
import com.ownstd.project.pincard.internal.presentation.viewmodel.ConstructorViewModel
import com.ownstd.project.storage.TokenStorage
import io.github.suwasto.capturablecompose.Capturable
import io.github.suwasto.capturablecompose.CompressionFormat
import io.github.suwasto.capturablecompose.rememberCaptureController
import io.github.suwasto.capturablecompose.toByteArray
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun LookConstructor(backClick: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<ConstructorViewModel>()

    val clotheList by viewModel.clotheList.collectAsState()
    val pickedClothes by viewModel.pickedClotheList.collectAsState()

    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val storage: TokenStorage = koinInject()
    val context = LocalPlatformContext.current
    val headers = NetworkHeaders.Builder()
        .set("Authorization", "Bearer ${storage.getToken()}")
        .build()
    val networkRepository: NetworkRepository = koinInject()
    val baseUrl = networkRepository.baseUrl
    val captureController = rememberCaptureController()

    Scaffold(
        modifier = Modifier.padding(bottom = 44.dp),
        topBar = { SimpleTopBar(backClick) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Capturable(
                captureController = captureController,
                onCaptured = { imageBitmap ->
                    val byteArray = imageBitmap.toByteArray(CompressionFormat.PNG, 100)
                    viewModel.save(byteArray)
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
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
            ModalBottomSheetLayout(
                sheetState = sheetState,
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x88000000))
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxWidth().background(Color.Transparent)
                        ) {
                            items(clotheList) { clothe ->
                                val request = ImageRequest.Builder(context)
                                    .data(clothe.imageUrl.parseToHost(baseUrl))
                                    .httpHeaders(headers)
                                    .build()
                                AsyncImage(
                                    model = request,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clickable { viewModel.pickClothe(clothe) }
                                )
                            }
                        }
                    }
                }
            ) {
                FloatingActionButton(
                    onClick = { scope.launch { sheetState.show() } },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) { Text(text = "+") }
                Button(
                    onClick = { viewModel.deleteLastInteracted() },
                    modifier = Modifier.align(Alignment.BottomStart)
                ) { Text("DELETE") }
                Button(
                    onClick = { captureController.capture() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) { Text("SAVE") }
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
    val storage: TokenStorage = koinInject()
    val context = LocalPlatformContext.current
    val headers = NetworkHeaders.Builder()
        .set("Authorization", "Bearer ${storage.getToken()}")
        .build()
    val networkRepository: NetworkRepository = koinInject()
    val baseUrl = networkRepository.baseUrl
    val request = ImageRequest.Builder(context)
        .data(item.imgUrl.parseToHost(baseUrl))
        .httpHeaders(headers)
        .build()
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
            model = request,
            modifier = Modifier
                .size(item.size)
                .graphicsLayer(rotationZ = item.rotation),
            contentDescription = "",
        )
    }
}


@Composable
fun SimpleTopBar(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = onClick) {
            Text(
                text = "<-",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.size(10.dp))
        }
    }

}
