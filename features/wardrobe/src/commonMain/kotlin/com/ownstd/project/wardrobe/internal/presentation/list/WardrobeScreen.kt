package com.ownstd.project.wardrobe.internal.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import pro.respawn.flowmvi.compose.dsl.subscribe
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.components.CategoryChips
import com.ownstd.project.core.compose.components.ItemCard
import com.ownstd.project.core.compose.components.SkeletonCard
import com.ownstd.project.core.compose.foundation.handle
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import com.ownstd.project.wardrobe.internal.domain.model.ClotheModel
import com.ownstd.project.wardrobe.internal.presentation.list.interactionModel.WardrobeIntent
import com.ownstd.project.wardrobe.internal.presentation.list.interactionModel.WardrobeSideEffect
import com.ownstd.project.wardrobe.internal.presentation.list.interactionModel.WardrobeState
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

private const val GRID_COLUMNS = 2
private const val SKELETON_ITEMS_COUNT = 6
private val BUTTON_CORNER_RADIUS = 12.dp
private val GRID_SPACING = 12.dp
private val GRID_PADDING = 16.dp

private val CATEGORIES = listOf("Все", "Верх", "Низ", "Обувь", "Верхняя", "Сумки")

@Composable
fun WardrobeScreen(
    onNavigateToDetail: (clotheId: Int) -> Unit,
    onShowConstructor: () -> Unit,
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
) {
    val viewModel = koinViewModel<WardrobeViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is WardrobeSideEffect.NavigateToDetail ->
                onNavigateToDetail(effect.clotheId)

            is WardrobeSideEffect.ShowAddBottomSheet ->
                showAddSheet = true

            is WardrobeSideEffect.ShowFilterBottomSheet ->
                showFilterSheet = true

            is WardrobeSideEffect.ShowError ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }

            is WardrobeSideEffect.ShowItemMenu -> Unit
        }
    }

    val state by viewModel.store.subscribe()

    WardrobeContent(
        state = state,
        snackbarHostState = snackbarHostState,
        showFilterSheet = showFilterSheet,
        showAddSheet = showAddSheet,
        onDismissFilter = { showFilterSheet = false },
        onDismissAdd = { showAddSheet = false },
        onCameraClick = { onCameraClick(); showAddSheet = false },
        onGalleryClick = { onGalleryClick(); showAddSheet = false },
        onConstructorClick = { onShowConstructor(); showAddSheet = false },
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun WardrobeContent(
    state: WardrobeState,
    snackbarHostState: SnackbarHostState,
    showFilterSheet: Boolean,
    showAddSheet: Boolean,
    onDismissFilter: () -> Unit,
    onDismissAdd: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onConstructorClick: () -> Unit,
    onIntent: (WardrobeIntent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            backgroundColor = Theme.colors.background.primary,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (!state.isEmpty) {
                    FloatingActionButton(
                        onClick = { onIntent(WardrobeIntent.FabClicked) },
                        backgroundColor = Theme.colors.component.fab.background,
                        contentColor = Theme.colors.component.fab.foreground,
                        shape = CircleShape,
                    ) {
                        Icon(
                            painter = painterResource(MR.images.plus),
                            contentDescription = "Добавить вещь",
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when {
                    state.isLoading -> WardrobeSkeletonGrid()
                    state.isEmpty -> WardrobeEmptyState(onIntent = onIntent)
                    else -> WardrobeClothingGrid(state = state, onIntent = onIntent)
                }
            }
        }

        FilterBottomSheet(
            visible = showFilterSheet,
            currentOptions = state.filterOptions,
            onApply = { options ->
                onIntent(WardrobeIntent.ApplyFilter(options))
                onDismissFilter()
            },
            onDismiss = onDismissFilter,
        )

        AddBottomSheet(
            visible = showAddSheet,
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick,
            onConstructorClick = onConstructorClick,
            onDismiss = onDismissAdd,
        )
    }
}

@Composable
private fun WardrobeClothingGrid(
    state: WardrobeState,
    onIntent: (WardrobeIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WardrobeHeader(
            count = state.filteredClothes.size,
            isFilterActive = state.isFilterActive,
            onFilterClick = { onIntent(WardrobeIntent.FilterClicked) },
        )
        CategoryChips(
            categories = CATEGORIES,
            activeCategory = state.activeCategory,
            onCategorySelect = { onIntent(WardrobeIntent.SelectCategory(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            contentPadding = PaddingValues(GRID_PADDING),
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
            verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.filteredClothes, key = { it.id ?: it.name }) { clothe ->
                ItemCard(
                    imageUrl = clothe.imageUrl,
                    name = clothe.name,
                    category = clothe.category,
                    onClick = { clothe.id?.let { onIntent(WardrobeIntent.ItemClicked(it)) } },
                    onLongClick = { clothe.id?.let { onIntent(WardrobeIntent.ItemLongClicked(it)) } },
                )
            }
        }
    }
}

@Composable
private fun WardrobeHeader(
    count: Int,
    isFilterActive: Boolean,
    onFilterClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GRID_PADDING, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$count вещей",
            style = Theme.typography.subhead,
            color = Theme.colors.label.secondary,
        )
        IconButton(onClick = onFilterClick) {
            Box {
                Icon(
                    painter = painterResource(MR.images.sliders_horizontal),
                    contentDescription = "Фильтр",
                    tint = if (isFilterActive) Theme.colors.accent.primary else Theme.colors.label.primary,
                )
                if (isFilterActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .background(Theme.colors.accent.primary, CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun WardrobeEmptyState(onIntent: (WardrobeIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(GRID_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(MR.images.shopping_bag),
            contentDescription = null,
            tint = Theme.colors.label.muted,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Гардероб пуст",
            style = Theme.typography.h2,
            color = Theme.colors.label.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Добавьте первую вещь, чтобы начать",
            style = Theme.typography.body,
            color = Theme.colors.label.secondary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onIntent(WardrobeIntent.FabClicked) },
            colors = ButtonDefaults.buttonColors(backgroundColor = Theme.colors.accent.primary),
            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Сфотографировать вещь",
                style = Theme.typography.body,
                color = Theme.colors.accent.onAccent,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onIntent(WardrobeIntent.FabClicked) },
            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Выбрать из галереи",
                style = Theme.typography.body,
                color = Theme.colors.label.primary,
            )
        }
    }
}

@Composable
private fun WardrobeSkeletonGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        contentPadding = PaddingValues(GRID_PADDING),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(SKELETON_ITEMS_COUNT) {
            SkeletonCard()
        }
    }
}

// region Previews

private fun previewClothe(id: Int, name: String, category: String) = ClotheModel(
    id = id,
    name = name,
    imageUrl = "",
    category = category,
    styles = null,
    season = null,
    color = null,
    size = null,
    tags = null,
    marketplaceLinks = emptyList(),
)

@Preview
@Composable
private fun WardrobeScreenPreview() {
    val clothes = listOf(
        previewClothe(1, "Белая футболка", "Верх"),
        previewClothe(2, "Чёрные джинсы", "Низ"),
        previewClothe(3, "Кроссовки", "Обувь"),
        previewClothe(4, "Куртка", "Верхняя"),
    )
    WardrobeContent(
        state = WardrobeState(clothes = clothes, filteredClothes = clothes, isLoading = false),
        snackbarHostState = SnackbarHostState(),
        showFilterSheet = false,
        showAddSheet = false,
        onDismissFilter = {},
        onDismissAdd = {},
        onCameraClick = {},
        onGalleryClick = {},
        onConstructorClick = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun WardrobeScreenEmptyPreview() {
    WardrobeContent(
        state = WardrobeState(isLoading = false, isEmpty = true),
        snackbarHostState = SnackbarHostState(),
        showFilterSheet = false,
        showAddSheet = false,
        onDismissFilter = {},
        onDismissAdd = {},
        onCameraClick = {},
        onGalleryClick = {},
        onConstructorClick = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun WardrobeScreenLoadingPreview() {
    WardrobeContent(
        state = WardrobeState(isLoading = true),
        snackbarHostState = SnackbarHostState(),
        showFilterSheet = false,
        showAddSheet = false,
        onDismissFilter = {},
        onDismissAdd = {},
        onCameraClick = {},
        onGalleryClick = {},
        onConstructorClick = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun WardrobeFilterSheetPreview() {
    WardrobeContent(
        state = WardrobeState(isLoading = false),
        snackbarHostState = SnackbarHostState(),
        showFilterSheet = true,
        showAddSheet = false,
        onDismissFilter = {},
        onDismissAdd = {},
        onCameraClick = {},
        onGalleryClick = {},
        onConstructorClick = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun WardrobeAddSheetPreview() {
    WardrobeContent(
        state = WardrobeState(isLoading = false),
        snackbarHostState = SnackbarHostState(),
        showFilterSheet = false,
        showAddSheet = true,
        onDismissFilter = {},
        onDismissAdd = {},
        onCameraClick = {},
        onGalleryClick = {},
        onConstructorClick = {},
        onIntent = {},
    )
}

// endregion
