package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import coil3.compose.AsyncImage
import com.ownstd.project.designsystem.components.*
import com.ownstd.project.designsystem.theme.ClothisTheme
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.presentation.viewmodel.*
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.*

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun ClothingDetailScreen(
    clotheId: Int,
    preloadedClothe: Clothe? = null,
    onBackClick: () -> Unit = {},
) {
    val viewModel: ClothingDetailViewModel = koinViewModel(key = "clothing-detail-$clotheId") {
        parametersOf(clotheId, preloadedClothe)
    }
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val latestOnBack by rememberUpdatedState(onBackClick)
    LaunchedEffect(viewModel) {
        viewModel.savedToWardrobeEvent.collect { latestOnBack() }
    }
    BackHandler(enabled = state.isEditMode || state.isSavingToWardrobe) {
        if (!state.isSaving && !state.isSavingToWardrobe) viewModel.onEditToggle()
    }
    ClothingDetailContent(
        state = state,
        onBack = {
            if (state.isEditMode) viewModel.onEditToggle() else onBackClick()
        },
        onEdit = viewModel::onEditToggle,
        onSave = viewModel::onSave,
        onAdd = viewModel::saveToWardrobe,
        onRetry = viewModel::retry,
        onFieldChange = viewModel::onFieldChange,
        onOpenStore = { url ->
            runCatching { uriHandler.openUri(url) }.onFailure { viewModel.onStoreOpenFailed() }
        },
    )
}

@Composable
internal fun ClothingDetailContent(
    state: ClothingDetailState,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onSave: () -> Unit = {},
    onAdd: () -> Unit = {},
    onRetry: () -> Unit = {},
    onFieldChange: (ClothingField, String) -> Unit = { _, _ -> },
    onOpenStore: (String) -> Unit = {},
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    val details = state.details
    val scrollState = rememberScrollState()
    LaunchedEffect(state.isEditMode) { scrollState.scrollTo(0) }
    LaunchedEffect(state.error, scrollState.maxValue) {
        if (state.error != null && details != null) scrollState.scrollTo(scrollState.maxValue)
    }
    Column(
        modifier = Modifier.fillMaxSize().background(colors.canvas)
            .statusBarsPadding().navigationBarsPadding().imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.widthIn(max = dimens.detailContentMaxWidth).fillMaxWidth()
                .padding(horizontal = dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                enabled = !state.isSaving && !state.isSavingToWardrobe,
                modifier = Modifier.size(dimens.touchTargetMin),
            ) {
                Icon(
                    painterResource(Res.drawable.ic_detail_back),
                    contentDescription = stringResource(Res.string.item_detail_back),
                    tint = colors.onCanvas,
                    modifier = Modifier.size(dimens.iconButtonIcon),
                )
            }
            Text(
                text = stringResource(if (state.isEditMode) Res.string.item_detail_edit_title else Res.string.item_detail_title),
                color = colors.onCanvas,
                style = ClothisTheme.typography.title,
                modifier = Modifier.weight(1f).padding(vertical = dimens.spaceSm),
            )
        }
        Column(
            modifier = Modifier.weight(1f).widthIn(max = dimens.detailContentMaxWidth)
                .fillMaxWidth().verticalScroll(scrollState)
                .padding(horizontal = dimens.screenHorizontalPadding)
                .padding(bottom = dimens.space2xl),
            verticalArrangement = Arrangement.spacedBy(dimens.space2xl),
        ) {
            when {
                details == null && state.isLoading -> ClothingDetailSkeleton()
                details == null -> {
                    Spacer(Modifier.height(dimens.space2xl))
                    DetailStatus(stringResource(errorResource(state.error ?: ClothingDetailError.Load)))
                    if (state.error == ClothingDetailError.NotFound) {
                        Text(
                            stringResource(Res.string.item_detail_not_found_hint),
                            color = colors.onCanvasMuted, style = ClothisTheme.typography.body,
                        )
                        ClothisPrimaryButton(stringResource(Res.string.item_detail_return), onBack, Modifier.fillMaxWidth())
                    } else {
                        ClothisPrimaryButton(stringResource(Res.string.item_detail_retry), onRetry, Modifier.fillMaxWidth())
                    }
                }
                state.isEditMode -> {
                    Text(
                        stringResource(Res.string.item_detail_edit_hint),
                        color = colors.onCanvasMuted, style = ClothisTheme.typography.body,
                    )
                    ClothingEditForm(state, onFieldChange)
                }
                else -> {
                    ClothingHero(details)
                    Column(verticalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                        details.category?.let {
                            Text(it, style = ClothisTheme.typography.caption, color = colors.onCanvasMuted)
                        }
                        Text(
                            details.name.ifBlank { stringResource(Res.string.item_detail_unnamed) },
                            style = ClothisTheme.typography.h1, color = colors.onCanvas,
                            modifier = Modifier.semantics { heading() },
                        )
                        if (state.isOwned != null) {
                            Text(
                                stringResource(if (state.isOwned) Res.string.item_detail_owned else Res.string.item_detail_catalog),
                                style = ClothisTheme.typography.caption, color = colors.onCanvasMuted,
                            )
                        }
                        if (state.didSave) DetailStatus(stringResource(Res.string.item_detail_saved))
                    }
                    ClothingAttributes(details)
                    MarketplaceSection(details, state.isOwned == true, onEdit, onOpenStore)
                    if (state.isOwned == true) {
                        Column(verticalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                            ClothisGhostButton(
                                text = stringResource(Res.string.item_detail_outfit),
                                onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                stringResource(Res.string.item_detail_outfit_unavailable),
                                style = ClothisTheme.typography.caption, color = colors.onCanvasMuted,
                            )
                        }
                    }
                }
            }
            // Long errors belong to the scrollable content, never a floating overlay.
            if (details != null && state.error != null) {
                DetailStatus(stringResource(errorResource(state.error)))
            }
        }
        if (details != null) {
            DetailActions(state, onEdit, onSave, onAdd, onRetry)
        }
    }
}

@Composable
private fun ClothingHero(details: ClothingDetailUiModel) {
    val dimens = ClothisTheme.dimens
    val colors = ClothisTheme.colors
    var imageFailed by remember(details.imageUrl) { mutableStateOf(false) }
    var imageLoaded by remember(details.imageUrl) { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.widthIn(max = dimens.detailImageMaxWidth).fillMaxWidth().aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(dimens.radiusCard)).background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            if (details.imageUrl.isBlank() || imageFailed || !imageLoaded) {
                Text(
                    stringResource(
                        if (details.imageUrl.isBlank() || imageFailed) Res.string.item_detail_image_missing
                        else Res.string.item_detail_image_loading,
                    ),
                    style = ClothisTheme.typography.body, color = colors.onSurface,
                    modifier = Modifier.padding(dimens.spaceLg),
                )
            }
            if (details.imageUrl.isNotBlank() && !imageFailed) {
                AsyncImage(
                    model = details.imageUrl,
                    contentDescription = details.name,
                    contentScale = ContentScale.Fit,
                    onSuccess = { imageLoaded = true },
                    onError = { imageFailed = true },
                    modifier = Modifier.fillMaxSize().padding(dimens.spaceLg),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClothingAttributes(details: ClothingDetailUiModel) {
    val dimens = ClothisTheme.dimens
    val colors = ClothisTheme.colors
    Column {
        SectionHeading(stringResource(Res.string.item_detail_about))
        details.attributes.forEach { attribute ->
            ClothisDetailRow(
                stringResource(fieldResource(attribute.field)),
                attribute.value ?: stringResource(Res.string.item_detail_missing),
            )
        }
        Column(
            modifier = Modifier.padding(top = dimens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        ) {
            Text(stringResource(Res.string.item_detail_colors), style = ClothisTheme.typography.caption, color = colors.onCanvasMuted)
            if (details.colors.isEmpty()) {
                Text(stringResource(Res.string.item_detail_missing), style = ClothisTheme.typography.body, color = colors.onCanvas)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm), verticalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                    details.colors.forEach { color ->
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(dimens.radiusChip)).background(colors.surfaceElevated)
                                .padding(dimens.spaceSm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm),
                        ) {
                            color.color?.let {
                                Box(Modifier.size(dimens.spaceLg).clip(CircleShape).background(it)
                                    .border(dimens.borderWidth, colors.onCanvasMuted, CircleShape))
                            }
                            Text(color.label, style = ClothisTheme.typography.caption, color = colors.onCanvas)
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier.padding(top = dimens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceSm),
        ) {
            Text(stringResource(Res.string.item_detail_style), style = ClothisTheme.typography.caption, color = colors.onCanvasMuted)
            if (details.tags.isEmpty()) {
                Text(stringResource(Res.string.item_detail_missing), style = ClothisTheme.typography.body, color = colors.onCanvas)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm), verticalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                    details.tags.forEach { tag ->
                        Text(
                            tag, style = ClothisTheme.typography.caption, color = colors.onCanvas,
                            modifier = Modifier.clip(RoundedCornerShape(dimens.radiusChip)).background(colors.surfaceElevated)
                                .padding(horizontal = dimens.spaceMd, vertical = dimens.spaceSm),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketplaceSection(
    details: ClothingDetailUiModel,
    isOwned: Boolean,
    onEdit: () -> Unit,
    onOpenStore: (String) -> Unit,
) {
    val colors = ClothisTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(ClothisTheme.dimens.spaceSm)) {
        SectionHeading(stringResource(Res.string.item_detail_store))
        if (details.storeUrl != null) {
            Text(details.storeDomain.orEmpty(), style = ClothisTheme.typography.body, color = colors.onCanvasMuted)
            ClothisGhostButton(
                stringResource(Res.string.item_detail_store_open), { onOpenStore(details.storeUrl) }, Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                stringResource(if (details.hasStoreValue) Res.string.item_detail_store_invalid else Res.string.item_detail_store_empty),
                style = ClothisTheme.typography.body, color = colors.onCanvasMuted,
            )
            if (isOwned) DetailTextAction(stringResource(Res.string.item_detail_store_add), onEdit)
        }
    }
}

@Composable
private fun ClothingEditForm(state: ClothingDetailState, onFieldChange: (ClothingField, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(ClothisTheme.dimens.spaceMd)) {
        ClothingField.entries.forEach { field ->
            ClothisTextField(
                value = state.draft[field].orEmpty(),
                onValueChange = { onFieldChange(field, it) },
                label = stringResource(fieldResource(field)),
                enabled = !state.isSaving,
                isError = (field == ClothingField.Name && state.error == ClothingDetailError.NameRequired) ||
                    (field == ClothingField.StoreUrl && state.error == ClothingDetailError.InvalidUrl),
                placeholder = when (field) {
                    ClothingField.Style -> stringResource(Res.string.item_detail_style_hint)
                    ClothingField.StoreUrl -> stringResource(Res.string.item_detail_store_hint)
                    else -> null
                },
                keyboardOptions = if (field == ClothingField.StoreUrl) KeyboardOptions(keyboardType = KeyboardType.Uri) else KeyboardOptions.Default,
            )
            if (field == ClothingField.Name && state.error == ClothingDetailError.NameRequired) {
                DetailStatus(stringResource(Res.string.item_detail_name_error))
            }
            if (field == ClothingField.StoreUrl && state.error == ClothingDetailError.InvalidUrl) {
                DetailStatus(stringResource(Res.string.item_detail_url_error))
            }
        }
    }
}

@Composable
private fun DetailActions(
    state: ClothingDetailState,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onAdd: () -> Unit,
    onRetry: () -> Unit,
) {
    val dimens = ClothisTheme.dimens
    Column(
        modifier = Modifier.widthIn(max = dimens.detailContentMaxWidth).fillMaxWidth()
            .padding(horizontal = dimens.screenHorizontalPadding)
            .padding(top = dimens.spaceSm, bottom = dimens.spaceSm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
    ) {
        Divider(color = ClothisTheme.colors.divider, thickness = dimens.borderWidth)
        when {
            state.isEditMode -> {
                ClothisPrimaryButton(
                    stringResource(if (state.isSaving) Res.string.item_detail_saving else Res.string.item_detail_save),
                    onSave, Modifier.fillMaxWidth(), isLoading = state.isSaving,
                )
                DetailTextAction(stringResource(Res.string.item_detail_cancel), onEdit, enabled = !state.isSaving)
            }
            state.isOwned == true -> ClothisPrimaryButton(stringResource(Res.string.item_detail_edit), onEdit, Modifier.fillMaxWidth())
            state.isOwned == false -> ClothisPrimaryButton(
                stringResource(if (state.isSavingToWardrobe) Res.string.item_detail_adding else Res.string.item_detail_add),
                onAdd, Modifier.fillMaxWidth(), isLoading = state.isSavingToWardrobe,
            )
            state.isLoading -> DetailStatus(stringResource(Res.string.item_detail_checking))
            else -> ClothisPrimaryButton(stringResource(Res.string.item_detail_retry), onRetry, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DetailTextAction(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.heightIn(min = ClothisTheme.dimens.touchTargetMin),
        colors = ButtonDefaults.textButtonColors(contentColor = ClothisTheme.colors.onCanvas, disabledContentColor = ClothisTheme.colors.onCanvasMuted),
    ) { Text(text, style = ClothisTheme.typography.body) }
}

@Composable
private fun SectionHeading(text: String) {
    Text(text, style = ClothisTheme.typography.h2, color = ClothisTheme.colors.onCanvas, modifier = Modifier.semantics { heading() })
}

@Composable
private fun DetailStatus(text: String) {
    Text(
        text, style = ClothisTheme.typography.body, color = ClothisTheme.colors.onCanvas,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
private fun ClothingDetailSkeleton() {
    val dimens = ClothisTheme.dimens
    val shimmer by rememberShimmerTranslation()
    Column(verticalArrangement = Arrangement.spacedBy(dimens.spaceLg)) {
        DetailStatus(stringResource(Res.string.item_detail_loading))
        SkeletonCard(shimmer, Modifier.fillMaxWidth().aspectRatio(4f / 3f), RoundedCornerShape(dimens.radiusCard))
        repeat(4) {
            SkeletonCard(shimmer, Modifier.fillMaxWidth().height(dimens.buttonHeight), RoundedCornerShape(dimens.radiusChip))
        }
    }
}

private fun fieldResource(field: ClothingField): StringResource = when (field) {
    ClothingField.Name -> Res.string.item_detail_name
    ClothingField.Season -> Res.string.item_detail_season
    ClothingField.Fit -> Res.string.item_detail_fit
    ClothingField.Material -> Res.string.item_detail_material
    ClothingField.Brand -> Res.string.item_detail_brand
    ClothingField.Occasion -> Res.string.item_detail_occasion
    ClothingField.Style -> Res.string.item_detail_style
    ClothingField.StoreUrl -> Res.string.item_detail_store_url
}

private fun errorResource(error: ClothingDetailError): StringResource = when (error) {
    ClothingDetailError.Load -> Res.string.item_detail_load_error
    ClothingDetailError.NotFound -> Res.string.item_detail_not_found
    ClothingDetailError.Ownership -> Res.string.item_detail_ownership_error
    ClothingDetailError.Save -> Res.string.item_detail_save_error
    ClothingDetailError.Add -> Res.string.item_detail_add_error
    ClothingDetailError.NameRequired -> Res.string.item_detail_name_error
    ClothingDetailError.InvalidUrl -> Res.string.item_detail_url_error
    ClothingDetailError.OpenStore -> Res.string.item_detail_open_error
}
