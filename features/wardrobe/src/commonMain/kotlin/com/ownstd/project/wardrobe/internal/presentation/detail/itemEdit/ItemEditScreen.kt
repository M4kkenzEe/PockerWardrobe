package com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import pro.respawn.flowmvi.compose.dsl.subscribe
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ownstd.project.core.compose.components.AppTopBar
import com.ownstd.project.core.compose.components.SizeSelector
import com.ownstd.project.core.compose.foundation.handle
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel.ItemEditIntent
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel.ItemEditSideEffect
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel.ItemEditState
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val SIZES = listOf("XXS", "XS", "S", "M", "L", "XL", "XXL")
private val PHOTO_CORNER = 16.dp
private val BUTTON_CORNER = 12.dp
private val SECTION_PADDING = 16.dp

@Composable
fun ItemEditScreen(
    clotheId: Int,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<ItemEditViewModel> { parametersOf(clotheId) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is ItemEditSideEffect.NavigateBack -> onBack()
            is ItemEditSideEffect.ShowError ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
        }
    }

    val state by viewModel.store.subscribe()

    ItemEditContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun ItemEditContent(
    state: ItemEditState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onIntent: (ItemEditIntent) -> Unit,
) {
    Scaffold(
        backgroundColor = Theme.colors.background.primary,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Изменить вещь",
                onBack = { onIntent(ItemEditIntent.CancelClicked) },
                trailingContent = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            color = Theme.colors.accent.primary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = { onIntent(ItemEditIntent.SaveClicked) }) {
                            Icon(
                                painter = painterResource(MR.images.check),
                                contentDescription = "Сохранить",
                                tint = Theme.colors.accent.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> ItemEditLoading(Modifier.padding(innerPadding))
            state.clothe != null -> ItemEditForm(
                clothe = state.clothe,
                isSaving = state.isSaving,
                modifier = Modifier.padding(innerPadding),
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun ItemEditLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Theme.colors.accent.primary)
    }
}

@Composable
private fun ItemEditForm(
    clothe: ClotheDetail,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
    onIntent: (ItemEditIntent) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
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
            EditSection(title = "Основные") {
                FormField(
                    label = "Название",
                    value = clothe.name,
                    onValueChange = { onIntent(ItemEditIntent.NameChanged(it)) },
                )
                FormField(
                    label = "Категория",
                    value = clothe.category ?: "",
                    onValueChange = { onIntent(ItemEditIntent.CategoryChanged(it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Размер",
                    style = Theme.typography.label,
                    color = Theme.colors.label.secondary,
                    modifier = Modifier.padding(horizontal = SECTION_PADDING, vertical = 4.dp),
                )
                SizeSelector(
                    sizes = SIZES,
                    selectedSize = clothe.size,
                    onSizeSelect = { onIntent(ItemEditIntent.SizeChanged(it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            EditSection(title = "Дополнительно") {
                FormField(
                    label = "Материал",
                    value = clothe.material ?: "",
                    onValueChange = { onIntent(ItemEditIntent.MaterialChanged(it)) },
                )
                FormField(
                    label = "Fit",
                    value = clothe.fit ?: "",
                    onValueChange = { onIntent(ItemEditIntent.FitChanged(it)) },
                )
                FormField(
                    label = "Стили",
                    value = clothe.styles?.joinToString(", ") ?: "",
                    onValueChange = { onIntent(ItemEditIntent.StylesChanged(it)) },
                    placeholder = "Casual, Minimal...",
                )
                FormField(
                    label = "Сезон",
                    value = clothe.season?.joinToString(", ") ?: "",
                    onValueChange = { onIntent(ItemEditIntent.SeasonChanged(it)) },
                    placeholder = "Весна, Лето...",
                )
                FormField(
                    label = "Бренд",
                    value = clothe.brand ?: "",
                    onValueChange = { onIntent(ItemEditIntent.BrandChanged(it)) },
                )
            }
        }

        item {
            EditSection(title = "Ссылки на маркетплейсы") {
                clothe.marketplaceLinks.forEachIndexed { index, url ->
                    MarketplaceLinkField(
                        url = url,
                        onUrlChange = { onIntent(ItemEditIntent.MarketplaceLinkChanged(index, it)) },
                        onRemove = { onIntent(ItemEditIntent.MarketplaceLinkRemoved(index)) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                AddLinkButton(
                    onClick = { onIntent(ItemEditIntent.AddMarketplaceLink) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Button(
                onClick = { onIntent(ItemEditIntent.SaveClicked) },
                enabled = !isSaving && clothe.name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Theme.colors.accent.primary,
                    disabledBackgroundColor = Theme.colors.background.surfaceAlt,
                ),
                shape = RoundedCornerShape(BUTTON_CORNER),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_PADDING, vertical = 8.dp),
            ) {
                Text(
                    text = "Сохранить изменения",
                    style = Theme.typography.body,
                    color = if (!isSaving && clothe.name.isNotBlank())
                        Theme.colors.accent.onAccent
                    else Theme.colors.label.muted,
                )
            }
        }
    }
}

@Composable
private fun EditSection(
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
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = Theme.typography.label,
                color = Theme.colors.label.secondary,
            )
        },
        placeholder = if (placeholder.isNotEmpty()) {
            {
                Text(
                    text = placeholder,
                    style = Theme.typography.body,
                    color = Theme.colors.label.muted,
                )
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Theme.colors.label.primary,
            backgroundColor = Theme.colors.background.surface,
            focusedBorderColor = Theme.colors.accent.primary,
            unfocusedBorderColor = Theme.colors.stroke.primary,
            cursorColor = Theme.colors.accent.primary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SECTION_PADDING, vertical = 4.dp),
    )
}

@Composable
private fun MarketplaceLinkField(
    url: String,
    onUrlChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SECTION_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = {
                Text(
                    text = "Ссылка",
                    style = Theme.typography.label,
                    color = Theme.colors.label.secondary,
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(MR.images.link_2),
                    contentDescription = null,
                    tint = Theme.colors.label.muted,
                    modifier = Modifier.size(18.dp),
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Theme.colors.label.primary,
                backgroundColor = Theme.colors.background.surface,
                focusedBorderColor = Theme.colors.accent.primary,
                unfocusedBorderColor = Theme.colors.stroke.primary,
                cursorColor = Theme.colors.accent.primary,
            ),
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(MR.images.x),
                contentDescription = "Удалить ссылку",
                tint = Theme.colors.label.muted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AddLinkButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SECTION_PADDING)
            .clip(RoundedCornerShape(12.dp))
            .background(Theme.colors.background.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(MR.images.plus),
            contentDescription = null,
            tint = Theme.colors.accent.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "Добавить ссылку",
            style = Theme.typography.body,
            color = Theme.colors.accent.primary,
        )
    }
}

// region Previews

private fun previewClothe() = ClotheDetail(
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
    marketplaceLinks = listOf("https://wildberries.ru/catalog/123"),
)

@Preview
@Composable
private fun ItemEditScreenPreview() {
    ItemEditContent(
        state = ItemEditState(clothe = previewClothe(), isLoading = false),
        snackbarHostState = SnackbarHostState(),
        onBack = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun ItemEditLoadingPreview() {
    ItemEditContent(
        state = ItemEditState(isLoading = true),
        snackbarHostState = SnackbarHostState(),
        onBack = {},
        onIntent = {},
    )
}

// endregion
