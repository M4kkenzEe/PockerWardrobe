package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import com.ownstd.project.pincard.internal.presentation.viewmodel.GenerateLooksError
import kotlinprojecttesting.features.wardrobe_feature.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GenerateLooksFeedback(
    error: GenerateLooksError,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
) {
    val message = when (error) {
        is GenerateLooksError.NotEnoughClothes -> missingLooksMessage(error.missing)
        GenerateLooksError.NetworkError -> Res.string.generate_looks_network_error
        GenerateLooksError.NoNewLooks -> Res.string.generate_looks_no_new
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onAction) {
                Text(stringResource(if (error is GenerateLooksError.NetworkError)
                    Res.string.wear_with_retry else Res.string.generate_looks_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.generate_looks_close)) }
        },
    )
}

internal fun missingLooksMessage(missing: List<MissingCategory>): StringResource = when (missing.toSet()) {
    setOf(MissingCategory.Top) -> Res.string.generate_looks_top
    setOf(MissingCategory.Bottom) -> Res.string.generate_looks_bottom
    setOf(MissingCategory.Shoes) -> Res.string.generate_looks_shoes
    setOf(MissingCategory.Top, MissingCategory.Bottom) -> Res.string.generate_looks_top_bottom
    setOf(MissingCategory.Top, MissingCategory.Shoes) -> Res.string.generate_looks_top_shoes
    setOf(MissingCategory.Bottom, MissingCategory.Shoes) -> Res.string.generate_looks_bottom_shoes
    setOf(MissingCategory.Top, MissingCategory.Bottom, MissingCategory.Shoes) -> Res.string.generate_looks_all
    else -> Res.string.generate_looks_generic
}
