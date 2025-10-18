package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookItem
import com.ownstd.project.pincard.internal.parseToHost
import com.ownstd.project.pincard.internal.presentation.viewmodel.LookDetailsViewModel
import com.ownstd.project.storage.TokenStorage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun LookDetailsScreen(
    lookId: Int? = null,
    shareToken: String? = null,
    onBackClick: () -> Unit = {}
) {
    val viewModel: LookDetailsViewModel = koinViewModel {
        parametersOf(lookId, shareToken)
    }
    val look by viewModel.look.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading || look == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val currentLook = look ?: return
    val listState = rememberLazyListState()

    // Вычисляем прозрачность карточки на основе скролла
    val cardAlpha by remember {
        derivedStateOf {
            val firstVisibleItemIndex = listState.firstVisibleItemIndex
            val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset

            if (firstVisibleItemIndex == 0) {
                // Карточка видна, но может быть частично прокручена
                1f - (firstVisibleItemScrollOffset / 1000f).coerceIn(0f, 1f)
            } else {
                // Карточка полностью прокручена
                0f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Белая карточка с превью образа
            item {
                Box(modifier = Modifier.padding(vertical = 16.dp)) {
                    LookPreviewCard(
                        look = currentLook,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(900.dp)
                            .padding(horizontal = 16.dp)
                            .graphicsLayer { alpha = cardAlpha }
                    )
                }
            }

            // Список вещей
            item {
                if (!currentLook.lookItems.isNullOrEmpty()) {
                    ClothesListSection(
                        lookItems = currentLook.lookItems,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }

        // TopBar с кнопками
        LookDetailsTopBar(
            lookName = currentLook.name,
            onBackClick = onBackClick,
            onAddToWardrobe = {
                if (shareToken != null) {
                    viewModel.addToWardrobe(shareToken)
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun LookDetailsTopBar(
    lookName: String,
    onBackClick: () -> Unit,
    onAddToWardrobe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка назад
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
        ) {
            Text("Назад")
        }

        // Название образа
        Text(
            text = lookName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Кнопка добавить в гардероб
        Button(
            onClick = onAddToWardrobe,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp)
        ) {

            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "В гардероб",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LookPreviewCard(
    look: Look,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        // Отрисовка вещей с их координатами
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            look.lookItems?.forEach { lookItem ->
                ClotheItemInLook(
                    lookItem = lookItem,
                    onItemClick = {
                        // TODO: Можно добавить действие при клике на вещь
                    }
                )
            }
        }
    }
}

@Composable
private fun ClotheItemInLook(
    lookItem: LookItem,
    onItemClick: () -> Unit = {}
) {
    val storage: TokenStorage = koinInject()
    val context = LocalPlatformContext.current
    val headers = NetworkHeaders.Builder()
        .set("Authorization", "Bearer ${storage.getToken()}")
        .build()
    val networkRepository: NetworkRepository = koinInject()
    val baseUrl = networkRepository.baseUrl

    val request = ImageRequest.Builder(context)
        .data(lookItem.clothe.imageUrl.parseToHost(baseUrl))
        .httpHeaders(headers)
        .build()

    Box(
        modifier = Modifier
            .offset { IntOffset(lookItem.x.roundToInt(), lookItem.y.roundToInt()) }
            .wrapContentSize()
            .zIndex(lookItem.z)
            .clickable { onItemClick() }
    ) {
        AsyncImage(
            model = request,
            contentDescription = lookItem.clothe.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(lookItem.size.dp)
                .graphicsLayer(rotationZ = lookItem.rotation)
        )
    }
}

@Composable
private fun ClothesListSection(
    lookItems: List<LookItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Вещи в образе",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height((lookItems.size / 2 + 1) * 200.dp) // Фиксированная высота для вложенной сетки
        ) {
            items(lookItems) { lookItem ->
                ClotheItemCard(clotheItem = lookItem)
            }
        }
    }
}

@Composable
private fun ClotheItemCard(
    clotheItem: LookItem
) {
    val storage: TokenStorage = koinInject()
    val context = LocalPlatformContext.current
    val headers = NetworkHeaders.Builder()
        .set("Authorization", "Bearer ${storage.getToken()}")
        .build()
    val networkRepository: NetworkRepository = koinInject()
    val baseUrl = networkRepository.baseUrl

    val request = ImageRequest.Builder(context)
        .data(clotheItem.clothe.imageUrl.parseToHost(baseUrl))
        .httpHeaders(headers)
        .build()

    Box(
        modifier = Modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable {
                // TODO: Можно добавить переход к деталям вещи или в магазин
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = request,
                contentDescription = clotheItem.clothe.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )

            if (clotheItem.clothe.name.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = clotheItem.clothe.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 2
                )
            }
        }
    }
}
