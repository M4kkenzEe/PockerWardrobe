package com.ownstd.project.outfit.internal.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import pro.respawn.flowmvi.compose.dsl.subscribe
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import com.ownstd.project.core.compose.components.AppTopBar
import com.ownstd.project.core.compose.components.ItemCard
import com.ownstd.project.core.compose.components.MoreMenu
import com.ownstd.project.core.compose.components.MoreMenuItem
import com.ownstd.project.core.compose.components.TagChip
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.painterResource
import com.ownstd.project.outfit.internal.domain.model.LookModel
import com.ownstd.project.outfit.internal.domain.model.LookItemModel
import com.ownstd.project.outfit.internal.presentation.detail.interactionModel.OutfitDetailIntent
import com.ownstd.project.outfit.internal.presentation.detail.interactionModel.OutfitDetailSideEffect
import com.ownstd.project.outfit.internal.presentation.detail.interactionModel.OutfitDetailState
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.ownstd.project.core.compose.foundation.handle

private val IMAGE_ASPECT_RATIO = 3f / 4f
private const val ITEM_GRID_COLUMNS = 2
private val GRID_SPACING = 12.dp
private val GRID_PADDING = 16.dp

@Composable
fun OutfitDetailScreen(
    lookId: Int,
    onBack: () -> Unit,
    onNavigateToConstructor: (lookId: Int) -> Unit,
    onNavigateToItem: (clotheId: Int) -> Unit,
) {
    val viewModel = koinViewModel<OutfitDetailViewModel> { parametersOf(lookId) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is OutfitDetailSideEffect.NavigateBack -> onBack()
            is OutfitDetailSideEffect.NavigateToConstructor ->
                onNavigateToConstructor(effect.lookId)
            is OutfitDetailSideEffect.NavigateToItem -> onNavigateToItem(effect.clotheId)
            is OutfitDetailSideEffect.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val state by viewModel.store.subscribe()

    OutfitDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun OutfitDetailContent(
    state: OutfitDetailState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onIntent: (OutfitDetailIntent) -> Unit,
) {
    var menuVisible by remember { mutableStateOf(false) }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onIntent(OutfitDetailIntent.DeleteCancelled) },
            title = { Text("Удалить образ?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = { onIntent(OutfitDetailIntent.DeleteConfirmed) }) {
                    Text("Удалить", color = Theme.colors.label.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(OutfitDetailIntent.DeleteCancelled) }) {
                    Text("Отмена")
                }
            },
        )
    }

    Scaffold(
        backgroundColor = Theme.colors.background.primary,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = state.look?.name ?: "",
                onBack = onBack,
                trailingContent = {
                    MoreMenu(
                        visible = menuVisible,
                        items = listOf(
                            MoreMenuItem(MR.images.pencil, "Редактировать") {
                                menuVisible = false
                                onIntent(OutfitDetailIntent.EditClicked)
                            },
                            MoreMenuItem(MR.images.bookmark, "В избранное") {
                                menuVisible = false
                                onIntent(OutfitDetailIntent.FavoriteClicked)
                            },
                            MoreMenuItem(MR.images.share_2, "Поделиться") {
                                menuVisible = false
                                onIntent(OutfitDetailIntent.ShareClicked)
                            },
                            MoreMenuItem(MR.images.trash_2, "Удалить", isDestructive = true) {
                                menuVisible = false
                                onIntent(OutfitDetailIntent.DeleteClicked)
                            },
                        ),
                        onDismiss = { menuVisible = false },
                        anchor = {
                            IconButton(onClick = { menuVisible = true }) {
                                Icon(
                                    painter = painterResource(MR.images.more_horizontal),
                                    contentDescription = null,
                                    tint = Theme.colors.label.primary,
                                )
                            }
                        },
                    )
                },
            )
        },
    ) { padding ->
        val look = state.look
        if (look != null) {
            val items = look.lookItems
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                item {
                    AsyncImage(
                        model = look.url,
                        contentDescription = look.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(IMAGE_ASPECT_RATIO)
                            .background(Theme.colors.background.surfaceAlt),
                    )
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = look.name,
                            style = Theme.typography.h3,
                            color = Theme.colors.label.primary,
                        )

                        val tags = look.tags
                        if (!tags.isNullOrEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tags) { tag ->
                                    TagChip(text = tag)
                                }
                            }
                        }
                    }
                }

                if (!items.isNullOrEmpty()) {
                    item {
                        Text(
                            text = "Вещи в образе",
                            style = Theme.typography.body,
                            color = Theme.colors.label.secondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    items(items.chunked(ITEM_GRID_COLUMNS)) { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = GRID_PADDING, vertical = GRID_SPACING / 2),
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ItemCard(
                                        imageUrl = item.imageUrl,
                                        name = item.name,
                                        category = item.category,
                                        onClick = { onIntent(OutfitDetailIntent.ItemClicked(item.clotheId)) },
                                    )
                                }
                            }
                            if (rowItems.size < ITEM_GRID_COLUMNS) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else if (!state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(
                    text = "Образ не найден",
                    style = Theme.typography.body,
                    color = Theme.colors.label.secondary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun OutfitDetailScreenPreview() {
    OutfitDetailContent(
        state = OutfitDetailState(
            isLoading = false,
            look = LookModel(
                id = 1,
                name = "Офисный образ",
                url = "",
                tags = listOf("Пальто", "Брюки", "Кеды"),
                lookItems = listOf(
                    LookItemModel(id = 1, clotheId = 10, imageUrl = "", name = "Пальто", category = "Верх"),
                    LookItemModel(id = 2, clotheId = 11, imageUrl = "", name = "Брюки", category = "Низ"),
                ),
            ),
        ),
        snackbarHostState = remember { SnackbarHostState() },
        onBack = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun OutfitDetailLoadingPreview() {
    OutfitDetailContent(
        state = OutfitDetailState(isLoading = true),
        snackbarHostState = remember { SnackbarHostState() },
        onBack = {},
        onIntent = {},
    )
}
