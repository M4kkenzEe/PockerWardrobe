package com.ownstd.project.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ownstd.project.designsystem.theme.ClothisTheme

@Composable
fun ClothisDetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(vertical = dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXs),
        ) {
            Text(label, style = ClothisTheme.typography.caption, color = colors.onCanvasMuted)
            Text(value, style = ClothisTheme.typography.body, color = colors.onCanvas)
        }
        Divider(color = colors.divider, thickness = dimens.borderWidth)
    }
}

@Composable
fun ClothisTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = ClothisTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = ClothisTheme.typography.body) },
        placeholder = placeholder?.let { { Text(it, style = ClothisTheme.typography.body) } },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        textStyle = ClothisTheme.typography.body,
        shape = RoundedCornerShape(ClothisTheme.dimens.radiusChip),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = colors.onCanvas,
            disabledTextColor = colors.onCanvasMuted,
            backgroundColor = colors.surfaceElevated,
            cursorColor = colors.clay,
            focusedBorderColor = colors.clay,
            unfocusedBorderColor = colors.onCanvasMuted,
            disabledBorderColor = colors.divider,
            focusedLabelColor = colors.onCanvas,
            unfocusedLabelColor = colors.onCanvasMuted,
            disabledLabelColor = colors.onCanvasMuted,
            placeholderColor = colors.onCanvasMuted,
            errorBorderColor = colors.error,
            errorLabelColor = colors.onCanvas,
            errorCursorColor = colors.clay,
        ),
    )
}
