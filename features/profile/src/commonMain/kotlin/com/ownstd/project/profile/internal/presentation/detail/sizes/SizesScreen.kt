package com.ownstd.project.profile.internal.presentation.detail.sizes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.components.AppTopBar
import com.ownstd.project.core.compose.components.SettingRow
import com.ownstd.project.core.compose.foundation.handle
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import com.ownstd.project.profile.internal.domain.model.UserSizes
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizeField
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizeRegion
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizesIntent
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizesSideEffect
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizesState
import org.koin.compose.viewmodel.koinViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe
import org.jetbrains.compose.ui.tooling.preview.Preview

private val primarySizeColors = mapOf(
    "XS" to Color(0xFFA78BFA),
    "S"  to Color(0xFF34D399),
    "M"  to Color(0xFF60A5FA),
    "L"  to Color(0xFFFBBF24),
    "XL" to Color(0xFFF87171),
)

@Composable
fun SizesScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<SizesViewModel>()

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is SizesSideEffect.ShowError -> Unit
        }
    }

    val state by viewModel.store.subscribe()

    SizesContent(
        state = state,
        onBack = onBack,
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun SizesContent(
    state: SizesState,
    onBack: () -> Unit,
    onIntent: (SizesIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.colors.background.primary),
    ) {
        AppTopBar(title = "Размеры", onBack = onBack)

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Theme.colors.label.primary)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Primary size badge
            state.sizes?.primarySize?.let { size ->
                SectionLabel("ВАШ РАЗМЕР")
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                primarySizeColors[size] ?: Theme.colors.accent.primary,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = size,
                            style = Theme.typography.h2,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Body params
            SectionLabel("ПАРАМЕТРЫ ТЕЛА")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Theme.colors.background.surface, RoundedCornerShape(12.dp)),
            ) {
                val fields = listOf(
                    SizeField.HEIGHT to ("Рост" to "${state.sizes?.height?.toInt() ?: "—"} см"),
                    SizeField.WEIGHT to ("Вес" to "${state.sizes?.weight?.toInt() ?: "—"} кг"),
                    SizeField.CHEST  to ("Обхват груди" to "${state.sizes?.chest?.toInt() ?: "—"} см"),
                    SizeField.WAIST  to ("Обхват талии" to "${state.sizes?.waist?.toInt() ?: "—"} см"),
                    SizeField.HIPS   to ("Обхват бёдер" to "${state.sizes?.hips?.toInt() ?: "—"} см"),
                )
                fields.forEachIndexed { idx, (field, pair) ->
                    val (label, value) = pair
                    SettingRow(
                        title = label,
                        subtitle = value,
                        icon = MR.images.chevron_right,
                        showDivider = idx < fields.lastIndex,
                        onClick = { onIntent(SizesIntent.EditField(field)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Region switcher
            SectionLabel("РАЗМЕРЫ ОДЕЖДЫ")
            RegionSwitcher(
                active = state.activeRegion,
                onSelect = { onIntent(SizesIntent.SelectRegion(it)) },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Theme.colors.background.surface, RoundedCornerShape(12.dp)),
            ) {
                val sizes = state.sizes
                val items = listOf(
                    "Верх"  to sizes?.toRegionSize(state.activeRegion, "top"),
                    "Низ"   to sizes?.toRegionSize(state.activeRegion, "bottom"),
                    "Обувь" to sizes?.toRegionSize(state.activeRegion, "shoe"),
                )
                items.forEachIndexed { idx, (label, value) ->
                    SettingRow(
                        title = label,
                        subtitle = value ?: "—",
                        icon = MR.images.chevron_right,
                        showDivider = idx < items.lastIndex,
                        onClick = {},
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Размеры используются для подбора одежды и рекомендаций от AI",
                style = Theme.typography.caption,
                color = Theme.colors.label.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Edit field dialog
    state.editingField?.let { field ->
        EditFieldDialog(
            field = field,
            currentValue = state.sizes?.fieldValue(field),
            onSave = { value -> onIntent(SizesIntent.SaveField(field, value)) },
            onDismiss = { onIntent(SizesIntent.DismissEdit) },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = Theme.typography.tiny,
        color = Theme.colors.label.muted,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun RegionSwitcher(
    active: SizeRegion,
    onSelect: (SizeRegion) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SizeRegion.entries.forEach { region ->
            val isActive = region == active
            Button(
                onClick = { onSelect(region) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isActive) Theme.colors.label.primary
                        else Theme.colors.background.surface,
                    contentColor = if (isActive) Theme.colors.background.primary
                        else Theme.colors.label.secondary,
                ),
                elevation = ButtonDefaults.elevation(0.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 4.dp, vertical = 8.dp
                ),
            ) {
                Text(text = region.name, style = Theme.typography.tiny)
            }
        }
    }
}

@Composable
private fun EditFieldDialog(
    field: SizeField,
    currentValue: Float?,
    onSave: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var input by remember { mutableStateOf(currentValue?.toString() ?: "") }
    val label = when (field) {
        SizeField.HEIGHT -> "Рост (см)"
        SizeField.WEIGHT -> "Вес (кг)"
        SizeField.CHEST  -> "Обхват груди (см)"
        SizeField.WAIST  -> "Обхват талии (см)"
        SizeField.HIPS   -> "Обхват бёдер (см)"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label, style = Theme.typography.h3) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Theme.colors.label.primary,
                    focusedBorderColor = Theme.colors.accent.primary,
                    unfocusedBorderColor = Theme.colors.stroke.primary,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                input.toFloatOrNull()?.let { onSave(it) }
            }) {
                Text("Сохранить", color = Theme.colors.accent.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Theme.colors.label.secondary)
            }
        },
    )
}

// Helpers
private fun UserSizes.fieldValue(field: SizeField): Float? = when (field) {
    SizeField.HEIGHT -> height
    SizeField.WEIGHT -> weight
    SizeField.CHEST  -> chest
    SizeField.WAIST  -> waist
    SizeField.HIPS   -> hips
}

private fun UserSizes.toRegionSize(region: SizeRegion, type: String): String? {
    // Placeholder — actual conversion would use CLOTHING_SIZES table
    return primarySize
}

@Preview
@Composable
private fun SizesScreenPreview() {
    SizesContent(
        state = SizesState(
            isLoading = false,
            sizes = UserSizes(primarySize = "S", height = 168f, weight = 58f),
        ),
        onBack = {},
        onIntent = {},
    )
}
