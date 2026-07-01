package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OccasionOption(val label: String, val value: String)

private val OCCASIONS = listOf(
    OccasionOption("Повседневное", "casual"),
    OccasionOption("Офис", "office"),
    OccasionOption("Спорт", "sport"),
    OccasionOption("Свидание", "date"),
    OccasionOption("Путешествие", "travel"),
    OccasionOption("Другое", "other"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OccasionPickerSheetContent(
    onOccasionSelected: (String) -> Unit,
    onSkip: () -> Unit
) {
    var selectedValue by remember { mutableStateOf<String?>(null) }
    var customText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Для какого случая эта вещь?",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OCCASIONS.forEach { option ->
                val isSelected = selectedValue == option.value
                Text(
                    text = option.label,
                    color = if (isSelected) Color(0xFF27272B) else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF8AB4F8) else Color(0xFF2E2E2E))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF8AB4F8) else Color(0xFF3E3E3E),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedValue = option.value }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        if (selectedValue == "other") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it },
                placeholder = { Text("Введите свой тег", color = Color(0xFF9AA0A6)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val tag = customText.trim()
                    if (tag.isNotEmpty()) onOccasionSelected(tag)
                }),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF8AB4F8),
                    unfocusedBorderColor = Color(0xFF3E3E3E),
                    cursorColor = Color(0xFF8AB4F8)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    selectedValue == "other" -> {
                        val tag = customText.trim()
                        if (tag.isNotEmpty()) onOccasionSelected(tag)
                        else onSkip()
                    }
                    selectedValue != null -> onOccasionSelected(selectedValue!!)
                    else -> onSkip()
                }
            },
            enabled = selectedValue != null,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF8AB4F8),
                disabledBackgroundColor = Color(0xFF3A3A3A)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Готово",
                color = if (selectedValue != null) Color(0xFF27272B) else Color(0xFF6E6E6E),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Пропустить",
                color = Color(0xFF9AA0A6),
                fontSize = 14.sp
            )
        }
    }
}
