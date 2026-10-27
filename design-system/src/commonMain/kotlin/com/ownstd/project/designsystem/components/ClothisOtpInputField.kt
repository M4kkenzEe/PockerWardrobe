package com.ownstd.project.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.designsystem.theme.ClothisTheme

@Composable
fun ClothisOtpInputField(
    code: String,
    onCodeChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(modifier = modifier) {
        Box {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceSm)) {
                repeat(6) { index ->
                    val digit = code.getOrNull(index)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { focusRequester.requestFocus() }
                            .border(
                                width = dimens.borderWidth,
                                color = when {
                                    error != null -> colors.error
                                    digit.isNotEmpty() -> colors.clay
                                    else -> colors.surfaceElevated
                                },
                                shape = RoundedCornerShape(dimens.radiusChip),
                            )
                            .background(colors.surfaceElevated, RoundedCornerShape(dimens.radiusChip)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = digit,
                            style = TextStyle(
                                color = colors.onCanvas,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                }
            }
            BasicTextField(
                value = TextFieldValue(text = code, selection = TextRange(code.length)),
                onValueChange = { newValue ->
                    val filtered = newValue.text.filter { it.isDigit() }.take(6)
                    onCodeChange(filtered)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .focusRequester(focusRequester),
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(dimens.spaceXs))
            Text(text = error, color = colors.error, fontSize = 12.sp)
        }
    }
}
