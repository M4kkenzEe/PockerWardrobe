package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.ownstd.project.designsystem.theme.ClothisTheme
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.presentation.viewmodel.*
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalResourceApi::class)
private fun previewItem() = Clothe(
    id = 1, name = "Прямые брюки", category = "Брюки",
    imageUrl = Res.getUri("drawable/preview_trousers.png"),
    season = "Весна, осень", fit = "Свободная", material = "Шерсть",
    brand = "COS", occasion = "На каждый день", styleTags = "Минимализм, повседневный",
    colors = listOf("#242424"), storeUrl = "https://www.cos.com/",
)

@Composable
private fun DetailPreview(edit: Boolean = false, owned: Boolean? = true, largeText: Boolean = false) {
    val item = remember { previewItem() }
    var state by remember {
        mutableStateOf(ClothingDetailState(
            clothe = item, details = item.toDetailUiModel(), draft = item.toEditFields(),
            isLoading = false, isOwned = owned, isEditMode = edit,
        ))
    }
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, if (largeText) 2f else density.fontScale)) {
        ClothisTheme {
            ClothingDetailContent(
                state,
                onEdit = { state = state.copy(isEditMode = !state.isEditMode, draft = item.toEditFields(), didSave = false) },
                onBack = { state = state.copy(isEditMode = false) },
                onFieldChange = { field, value -> state = state.copy(draft = state.draft + (field to value)) },
                onSave = {
                    val updated = item.copy(name = state.draft[ClothingField.Name].orEmpty())
                    state = state.copy(clothe = updated, details = updated.toDetailUiModel(), isEditMode = false, didSave = true)
                },
            )
        }
    }
}

@Preview
@Composable
fun ClothingDetailOwnedPreview() = DetailPreview()

@Preview
@Composable
fun ClothingDetailEditPreview() = DetailPreview(edit = true)

@Preview
@Composable
fun ClothingDetailLargeTextPreview() = DetailPreview(edit = true, largeText = true)

@Preview
@Composable
fun ClothingDetailCatalogPreview() = DetailPreview(owned = false)

@Preview
@Composable
fun ClothingDetailLoadingPreview() {
    ClothisTheme { ClothingDetailContent(ClothingDetailState()) }
}

@Preview
@Composable
fun ClothingDetailErrorPreview() {
    ClothisTheme { ClothingDetailContent(ClothingDetailState(isLoading = false, error = ClothingDetailError.Load)) }
}

@Preview
@Composable
fun ClothingDetailPartialPreview() {
    val item = remember { Clothe(id = 1, name = "Длинное название вещи с дополнительным описанием фасона, материала и коллекции", imageUrl = "") }
    ClothisTheme {
        ClothingDetailContent(ClothingDetailState(clothe = item, details = item.toDetailUiModel(), isLoading = false, isOwned = true))
    }
}
