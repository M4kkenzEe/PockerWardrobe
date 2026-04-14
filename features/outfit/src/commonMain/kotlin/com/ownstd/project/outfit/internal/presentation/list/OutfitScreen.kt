package com.ownstd.project.outfit.internal.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import pro.respawn.flowmvi.compose.dsl.subscribe
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.components.CategoryChips
import com.ownstd.project.core.compose.components.LookCard
import com.ownstd.project.core.compose.components.MoreMenuItem
import com.ownstd.project.core.compose.components.SkeletonCard
import com.ownstd.project.core.compose.foundation.handle
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import com.ownstd.project.outfit.internal.domain.model.LookModel
import com.ownstd.project.outfit.internal.presentation.list.interactionModel.OutfitIntent
import com.ownstd.project.outfit.internal.presentation.list.interactionModel.OutfitSideEffect
import com.ownstd.project.outfit.internal.presentation.list.interactionModel.OutfitState
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

private const val GRID_COLUMNS = 2
private const val SKELETON_ITEMS_COUNT = 6
private val GRID_SPACING = 12.dp
private val GRID_PADDING = 16.dp

private val STYLES = listOf("Все", "Casual", "Smart Casual", "Business", "Sport", "Evening")

@Composable
fun OutfitScreen(
    onNavigateToDetail: (lookId: Int) -> Unit,
    onNavigateToConstructor: () -> Unit,
) {
    val viewModel = koinViewModel<OutfitViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is OutfitSideEffect.NavigateToDetail -> onNavigateToDetail(effect.lookId)
            is OutfitSideEffect.NavigateToConstructor -> onNavigateToConstructor()
            is OutfitSideEffect.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val state by viewModel.store.subscribe()

    OutfitContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun OutfitContent(
    state: OutfitState,
    snackbarHostState: SnackbarHostState,
    onIntent: (OutfitIntent) -> Unit,
) {
    var menuLookId by remember { mutableIntStateOf(-1) }

    Scaffold(
        backgroundColor = Theme.colors.background.primary,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(OutfitIntent.FabClicked) },
                backgroundColor = Theme.colors.accent.primary,
                shape = CircleShape,
            ) {
                Icon(
                    painter = painterResource(MR.images.plus),
                    contentDescription = null,
                    tint = Theme.colors.accent.onAccent,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CategoryChips(
                categories = STYLES,
                activeCategory = state.activeStyle,
                onCategorySelect = { style ->
                    onIntent(OutfitIntent.SelectStyle(style))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )

            when {
                state.isLoading -> {
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

                state.isEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Нет образов",
                            style = Theme.typography.body,
                            color = Theme.colors.label.secondary,
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GRID_COLUMNS),
                        contentPadding = PaddingValues(GRID_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
                        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.looks, key = { it.id ?: it.name }) { look ->
                            val lookId = look.id ?: return@items
                            LookCard(
                                imageUrl = look.url,
                                name = look.name,
                                isLocked = look.isLocked,
                                onClick = { onIntent(OutfitIntent.LookClicked(lookId)) },
                                onMoreClick = { menuLookId = lookId },
                                menuVisible = menuLookId == lookId,
                                onMenuDismiss = { menuLookId = -1 },
                                menuItems = lookMenuItems(
                                    onFavorite = {
                                        menuLookId = -1
                                        onIntent(OutfitIntent.FavoriteLook(lookId))
                                    },
                                    onShare = {
                                        menuLookId = -1
                                        onIntent(OutfitIntent.ShareLook(lookId))
                                    },
                                    onDelete = {
                                        menuLookId = -1
                                        onIntent(OutfitIntent.DeleteLook(lookId))
                                    },
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun lookMenuItems(
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
): List<MoreMenuItem> = listOf(
    MoreMenuItem(MR.images.bookmark, "В избранное", action = onFavorite),
    MoreMenuItem(MR.images.share_2, "Поделиться", action = onShare),
    MoreMenuItem(MR.images.trash_2, "Удалить", isDestructive = true, action = onDelete),
)

@Preview
@Composable
private fun OutfitScreenLoadingPreview() {
    OutfitContent(
        state = OutfitState(isLoading = true),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
    )
}

@Preview
@Composable
private fun OutfitScreenEmptyPreview() {
    OutfitContent(
        state = OutfitState(isLoading = false, isEmpty = true),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
    )
}

@Preview
@Composable
private fun OutfitScreenNormalPreview() {
    OutfitContent(
        state = OutfitState(
            isLoading = false,
            looks = listOf(
                LookModel(id = 1, name = "Офисный образ", url = ""),
                LookModel(id = 2, name = "Casual выходной", url = ""),
            ),
        ),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
    )
}
