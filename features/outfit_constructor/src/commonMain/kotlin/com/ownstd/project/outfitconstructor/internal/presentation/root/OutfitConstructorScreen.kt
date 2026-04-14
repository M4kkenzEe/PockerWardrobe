package com.ownstd.project.outfitconstructor.internal.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import pro.respawn.flowmvi.compose.dsl.subscribe
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ownstd.project.core.compose.components.AppBottomSheet
import com.ownstd.project.core.compose.components.AppTopBar
import com.ownstd.project.core.compose.components.CategoryChips
import com.ownstd.project.core.compose.components.ItemCard
import com.ownstd.project.core.compose.foundation.handle
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import com.ownstd.project.outfitconstructor.internal.domain.model.ClotheModel
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.CanvasItem
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.OutfitConstructorIntent
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.OutfitConstructorSideEffect
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.OutfitConstructorState
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private val GRID_SPACING = 8.dp
private val PICKER_GRID_PADDING = 12.dp
private const val PICKER_GRID_COLUMNS = 3

@Composable
fun OutfitConstructorScreen(
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<OutfitConstructorViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is OutfitConstructorSideEffect.NavigateBack -> onBack()
            is OutfitConstructorSideEffect.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val state by viewModel.store.subscribe()

    OutfitConstructorContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun OutfitConstructorContent(
    state: OutfitConstructorState,
    snackbarHostState: SnackbarHostState,
    onIntent: (OutfitConstructorIntent) -> Unit,
) {
    val categories = remember(state.availableClothes) {
        listOf("Все") + state.availableClothes.mapNotNull { it.category }.distinct()
    }

    Scaffold(
        backgroundColor = Theme.colors.background.primary,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Конструктор",
                onBack = { onIntent(OutfitConstructorIntent.BackClicked) },
                trailingContent = {
                    TextButton(
                        onClick = { onIntent(OutfitConstructorIntent.ShowPicker) },
                    ) {
                        Icon(
                            painter = painterResource(MR.images.plus),
                            contentDescription = "Добавить вещь",
                            tint = Theme.colors.accent.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Вещь",
                            style = Theme.typography.body,
                            color = Theme.colors.accent.primary,
                        )
                    }
                },
            )
        },
        bottomBar = {
            ConstructorBottomBar(
                state = state,
                onIntent = onIntent,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ConstructorCanvas(
                items = state.canvasItems,
                onTapEmpty = { onIntent(OutfitConstructorIntent.SelectItem(null)) },
                onItemTap = { id -> onIntent(OutfitConstructorIntent.SelectItem(id)) },
                onItemMove = { id, x, y -> onIntent(OutfitConstructorIntent.MoveItem(id, x, y)) },
                onItemScale = { id, scale -> onIntent(OutfitConstructorIntent.ScaleItem(id, scale)) },
            )

            if (state.canvasItems.isEmpty()) {
                Text(
                    text = "Добавьте вещи из гардероба",
                    style = Theme.typography.body,
                    color = Theme.colors.label.secondary,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    AppBottomSheet(
        visible = state.isPickerVisible,
        onDismiss = { onIntent(OutfitConstructorIntent.HidePicker) },
        title = "Выберите вещи",
    ) {
        CategoryChips(
            categories = categories,
            activeCategory = state.activeCategory,
            onCategorySelect = { cat ->
                onIntent(OutfitConstructorIntent.FilterCategory(cat))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(PICKER_GRID_COLUMNS),
            contentPadding = PaddingValues(PICKER_GRID_PADDING),
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
            verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
        ) {
            items(state.filteredClothes, key = { it.id ?: it.name }) { clothe ->
                val isAdded = clothe.id in state.addedClotheIds
                ItemCard(
                    imageUrl = clothe.imageUrl,
                    name = clothe.name,
                    category = clothe.category,
                    onClick = {
                        if (!isAdded) onIntent(OutfitConstructorIntent.AddItem(clothe))
                    },
                    isSelected = isAdded,
                    selectionMode = isAdded,
                    modifier = if (isAdded) Modifier.then(
                        Modifier.background(
                            color = Color.Black.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                        )
                    ) else Modifier,
                )
            }
        }
    }
}

@Composable
private fun ConstructorCanvas(
    items: List<CanvasItem>,
    onTapEmpty: () -> Unit,
    onItemTap: (Int) -> Unit,
    onItemMove: (Int, Float, Float) -> Unit,
    onItemScale: (Int, Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Theme.colors.stroke.primary,
                shape = RoundedCornerShape(0.dp),
            )
            .pointerInput(Unit) {
                detectTapGestures { onTapEmpty() }
            },
    ) {
        items.forEach { item ->
            val clotheId = item.clothe.id ?: return@forEach
            CanvasItemView(
                item = item,
                onTap = { onItemTap(clotheId) },
                onMove = { dx, dy -> onItemMove(clotheId, item.x + dx, item.y + dy) },
                onScale = { scale -> onItemScale(clotheId, item.scale * scale) },
            )
        }
    }
}

@Composable
private fun CanvasItemView(
    item: CanvasItem,
    onTap: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onScale: (Float) -> Unit,
) {
    val itemSize = (120 * item.scale).dp

    Box(
        modifier = Modifier
            .offset { IntOffset(item.x.roundToInt(), item.y.roundToInt()) }
            .size(itemSize)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (item.isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Theme.colors.accent.primary,
                        shape = RoundedCornerShape(8.dp),
                    )
                } else Modifier
            )
            .pointerInput(item.clothe.id) {
                detectTapGestures { onTap() }
            }
            .pointerInput(item.clothe.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onMove(dragAmount.x, dragAmount.y)
                }
            }
            .pointerInput(item.clothe.id) {
                detectTransformGestures { _, _, zoom, _ ->
                    onScale(zoom)
                }
            },
    ) {
        AsyncImage(
            model = item.clothe.imageUrl,
            contentDescription = item.clothe.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ConstructorBottomBar(
    state: OutfitConstructorState,
    onIntent: (OutfitConstructorIntent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Theme.colors.background.primary)
            .border(
                width = 1.dp,
                color = Theme.colors.stroke.primary,
                shape = RoundedCornerShape(0.dp),
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val selectedId = state.selectedItem?.clothe?.id

        if (selectedId != null) {
            IconButton(onClick = {
                val scale = state.selectedItem?.scale ?: 1f
                onIntent(OutfitConstructorIntent.ScaleItem(selectedId, scale / 1.2f))
            }) {
                Icon(
                    painter = painterResource(MR.images.zoom_out),
                    contentDescription = "Уменьшить",
                    tint = Theme.colors.label.primary,
                )
            }
            IconButton(onClick = {
                val scale = state.selectedItem?.scale ?: 1f
                onIntent(OutfitConstructorIntent.ScaleItem(selectedId, scale * 1.2f))
            }) {
                Icon(
                    painter = painterResource(MR.images.zoom_in),
                    contentDescription = "Увеличить",
                    tint = Theme.colors.label.primary,
                )
            }
            IconButton(onClick = {
                onIntent(OutfitConstructorIntent.RemoveItem(selectedId))
            }) {
                Icon(
                    painter = painterResource(MR.images.trash_2),
                    contentDescription = "Удалить",
                    tint = Theme.colors.label.error,
                )
            }
        } else {
            IconButton(onClick = { onIntent(OutfitConstructorIntent.ResetCanvas) }) {
                Icon(
                    painter = painterResource(MR.images.rotate_ccw),
                    contentDescription = "Сбросить холст",
                    tint = Theme.colors.label.primary,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        val itemCount = state.canvasItems.size
        if (itemCount > 0) {
            Text(
                text = "$itemCount",
                style = Theme.typography.body,
                color = Theme.colors.label.secondary,
                modifier = Modifier.padding(end = 8.dp),
            )
        }

        IconButton(
            onClick = {
                if (state.canSave && !state.isSaving) {
                    onIntent(OutfitConstructorIntent.SaveClicked { ByteArray(0) })
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (state.canSave) Theme.colors.accent.primary
                    else Theme.colors.component.skeleton,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                painter = painterResource(MR.images.check),
                contentDescription = "Сохранить",
                tint = if (state.canSave) Theme.colors.accent.onAccent
                else Theme.colors.label.secondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview
@Composable
private fun OutfitConstructorEmptyPreview() {
    OutfitConstructorContent(
        state = OutfitConstructorState(),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
    )
}

@Preview
@Composable
private fun OutfitConstructorWithItemsPreview() {
    val clothe = ClotheModel(id = 1, name = "Пальто", imageUrl = "", category = "Верх")
    OutfitConstructorContent(
        state = OutfitConstructorState(
            canvasItems = listOf(
                CanvasItem(clothe = clothe, x = 50f, y = 80f, isSelected = true),
            ),
        ),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
    )
}
