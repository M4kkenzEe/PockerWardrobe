package com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ownstd.project.core.compose.components.AppTopBar
import com.ownstd.project.core.compose.components.MarketplaceLinkRow
import com.ownstd.project.core.compose.components.TagChip
import com.ownstd.project.core.compose.foundation.handle
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail
import com.ownstd.project.wardrobe.internal.domain.model.Look
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel.ItemDetailIntent
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel.ItemDetailSideEffect
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel.ItemDetailState
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val PHOTO_CORNER = 16.dp
private val BUTTON_CORNER = 12.dp
private val SECTION_PADDING = 16.dp

@Composable
fun ItemDetailScreen(
    clotheId: Int,
    onBack: () -> Unit,
    onNavigateToEdit: (clotheId: Int) -> Unit,
    onNavigateToLook: (lookId: Int) -> Unit,
    onOpenUrl: (url: String) -> Unit,
) {
    val viewModel = koinViewModel<ItemDetailViewModel> { parametersOf(clotheId) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is ItemDetailSideEffect.NavigateBack -> onBack()
            is ItemDetailSideEffect.NavigateToEdit -> onNavigateToEdit(effect.clotheId)
            is ItemDetailSideEffect.NavigateToLook -> onNavigateToLook(effect.lookId)
            is ItemDetailSideEffect.OpenUrl -> onOpenUrl(effect.url)
            is ItemDetailSideEffect.ShowError ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
        }
    }

    val state by viewModel.store.subscribe()

    ItemDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun ItemDetailContent(
    state: ItemDetailState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onIntent: (ItemDetailIntent) -> Unit,
) {
    Scaffold(
        backgroundColor = Theme.colors.background.primary,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = state.clothe?.name ?: "Вещь",
                onBack = onBack,
                trailingContent = {
                    IconButton(onClick = { onIntent(ItemDetailIntent.EditClicked) }) {
                        Icon(
                            painter = painterResource(MR.images.pencil),
                            contentDescription = "Редактировать",
                            tint = Theme.colors.label.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> ItemDetailLoading(Modifier.padding(innerPadding))
            state.clothe != null -> ItemDetailBody(
                clothe = state.clothe,
                relatedLooks = state.relatedLooks,
                modifier = Modifier.padding(innerPadding),
                onIntent = onIntent,
            )
        }
    }

    if (state.showDeleteConfirm) {
        DeleteConfirmDialog(
            onConfirm = { onIntent(ItemDetailIntent.DeleteConfirmed) },
            onDismiss = { onIntent(ItemDetailIntent.DeleteCancelled) },
        )
    }
}

@Composable
private fun ItemDetailLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Theme.colors.accent.primary)
    }
}

@Composable
private fun ItemDetailBody(
    clothe: ClotheDetail,
    relatedLooks: List<Look>,
    modifier: Modifier = Modifier,
    onIntent: (ItemDetailIntent) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
    ) {
        item {
            AsyncImage(
                model = clothe.imageUrl,
                contentDescription = clothe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING, vertical = 12.dp)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(PHOTO_CORNER))
                    .background(Theme.colors.background.surfaceAlt),
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = clothe.name,
                    style = Theme.typography.h3,
                    color = Theme.colors.label.primary,
                    modifier = Modifier.weight(1f),
                )
                if (clothe.category != null) {
                    TagChip(text = clothe.category)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            DetailSection(title = "Характеристики") {
                DetailRow("Категория", clothe.category)
                DetailRow("Материал", clothe.material)
                DetailRow("Fit", clothe.fit)
                DetailRow("Стили", clothe.styles?.joinToString(" · "))
                DetailRow("Сезон", clothe.season?.joinToString(" · "))
                DetailRow("Цвет", clothe.color)
                DetailRow("Бренд", clothe.brand)
                DetailRow("Размер", clothe.size)
                DetailRow("Добавлено", clothe.createdAt, isLast = true)
            }
        }

        if (!clothe.tags.isNullOrEmpty()) {
            item {
                DetailSection(title = "Теги") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = SECTION_PADDING),
                    ) {
                        items(clothe.tags) { tag -> TagChip(text = tag) }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (relatedLooks.isNotEmpty()) {
            item {
                DetailSection(title = "Используется в образах") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = SECTION_PADDING),
                    ) {
                        items(relatedLooks) { look ->
                            LookChip(
                                look = look,
                                onClick = { onIntent(ItemDetailIntent.LookClicked(look.id)) },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (clothe.marketplaceLinks.isNotEmpty()) {
            item {
                DetailSection(title = "Ссылки на маркетплейсы") {
                    Column(
                        modifier = Modifier.padding(horizontal = SECTION_PADDING),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        clothe.marketplaceLinks.forEach { url ->
                            MarketplaceLinkRow(
                                url = url,
                                onClick = { onIntent(ItemDetailIntent.MarketplaceLinkClicked(url)) },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { onIntent(ItemDetailIntent.EditClicked) },
                    shape = RoundedCornerShape(BUTTON_CORNER),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Изменить",
                        style = Theme.typography.body,
                        color = Theme.colors.label.primary,
                    )
                }
                Button(
                    onClick = { onIntent(ItemDetailIntent.DeleteClicked) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Theme.colors.label.error,
                    ),
                    shape = RoundedCornerShape(BUTTON_CORNER),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Удалить",
                        style = Theme.typography.body,
                        color = Theme.colors.label.onError,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            style = Theme.typography.subhead,
            color = Theme.colors.label.primary,
            modifier = Modifier.padding(horizontal = SECTION_PADDING, vertical = 8.dp),
        )
        Divider(
            color = Theme.colors.stroke.primary,
            modifier = Modifier.padding(horizontal = SECTION_PADDING),
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
    isLast: Boolean = false,
) {
    if (value == null) return
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SECTION_PADDING, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = Theme.typography.body,
                color = Theme.colors.label.secondary,
            )
            Text(
                text = value,
                style = Theme.typography.body,
                color = Theme.colors.label.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(start = 16.dp),
            )
        }
        if (!isLast) {
            Divider(
                color = Theme.colors.stroke.primary,
                modifier = Modifier.padding(horizontal = SECTION_PADDING),
            )
        }
    }
}

@Composable
private fun LookChip(
    look: Look,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Theme.colors.background.surface)
            .border(1.dp, Theme.colors.stroke.primary, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(MR.images.sparkles),
            contentDescription = null,
            tint = Theme.colors.accent.primary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = look.name,
            style = Theme.typography.label,
            color = Theme.colors.label.primary,
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Удалить вещь?",
                style = Theme.typography.h3,
                color = Theme.colors.label.primary,
            )
        },
        text = {
            Text(
                text = "Эта вещь будет удалена из вашего гардероба. Это действие нельзя отменить.",
                style = Theme.typography.body,
                color = Theme.colors.label.secondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Удалить",
                    style = Theme.typography.body,
                    color = Theme.colors.label.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Отмена",
                    style = Theme.typography.body,
                    color = Theme.colors.label.secondary,
                )
            }
        },
        backgroundColor = Theme.colors.background.surface,
    )
}

// region Previews

private fun previewClotheDetail() = ClotheDetail(
    id = 1,
    name = "Белая льняная рубашка",
    imageUrl = "",
    category = "Верх",
    material = "Лён",
    fit = "Regular",
    styles = listOf("Casual", "Minimal"),
    season = listOf("Весна", "Лето"),
    color = "Белый",
    brand = "H&M",
    size = "M",
    marketplaceLinks = listOf("https://wildberries.ru/catalog/123", "https://ozon.ru/product/456"),
    tags = listOf("casual look", "minimalist style"),
    createdAt = "12 мар 2026",
)

@Preview
@Composable
private fun ItemDetailScreenPreview() {
    ItemDetailContent(
        state = ItemDetailState(
            clothe = previewClotheDetail(),
            relatedLooks = listOf(Look(1, "Осенний образ"), Look(2, "Минимал Стиль")),
            isLoading = false,
        ),
        snackbarHostState = SnackbarHostState(),
        onBack = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun ItemDetailLoadingPreview() {
    ItemDetailContent(
        state = ItemDetailState(isLoading = true),
        snackbarHostState = SnackbarHostState(),
        onBack = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun ItemDetailDeleteDialogPreview() {
    ItemDetailContent(
        state = ItemDetailState(
            clothe = previewClotheDetail(),
            isLoading = false,
            showDeleteConfirm = true,
        ),
        snackbarHostState = SnackbarHostState(),
        onBack = {},
        onIntent = {},
    )
}

// endregion
