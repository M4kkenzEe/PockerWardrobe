package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.presentation.viewmodel.ClothingDetailViewModel
import com.ownstd.project.pincard.internal.replaceFragment
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val BG = Color(0xFF1E1E1E)
private val SURFACE = Color(0xFF2A2A2A)
private val ACCENT = Color(0xFF8AB4F8)
private val TEXT_PRIMARY = Color.White
private val TEXT_SECONDARY = Color(0xFF9AA0A6)
private val CHIP_BG = Color(0xFF3A3A3A)

@Composable
fun ClothingDetailScreen(
    clotheId: Int,
    preloadedClothe: Clothe? = null,
    onBackClick: () -> Unit = {},
) {
    val viewModel: ClothingDetailViewModel = koinViewModel { parametersOf(clotheId, preloadedClothe) }
    val state by viewModel.state.collectAsState()
    val editName by viewModel.editName.collectAsState()
    val editStoreUrl by viewModel.editStoreUrl.collectAsState()
    val editSeason by viewModel.editSeason.collectAsState()
    val editFit by viewModel.editFit.collectAsState()
    val editMaterial by viewModel.editMaterial.collectAsState()
    val editBrand by viewModel.editBrand.collectAsState()
    val editStyleTags by viewModel.editStyleTags.collectAsState()

    val clothe = state.clothe
    val isEditMode = state.isEditMode
    val isSaving = state.isSaving
    val uriHandler = LocalUriHandler.current

    Box(modifier = Modifier.fillMaxSize().background(BG)) {
        if (clothe == null && state.error == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = ACCENT
            )
        } else if (state.error != null && clothe == null) {
            Text(
                text = state.error ?: "Ошибка",
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (clothe != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Hero image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    AsyncImage(
                        model = clothe.imageUrl.replaceFragment(),
                        contentDescription = clothe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Top bar overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .clickable { onBackClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("←", color = TEXT_PRIMARY, fontSize = 18.sp)
                        }
                        if (!viewModel.isReadOnly) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = ACCENT,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x99000000))
                                        .clickable {
                                            if (isEditMode) viewModel.onSave() else viewModel.onEditToggle()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEditMode) "✓" else "✏",
                                        color = if (isEditMode) ACCENT else TEXT_PRIMARY,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }
                }

                // Scrollable attributes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Name
                    AttributeSection(label = "Название") {
                        if (isEditMode) {
                            EditField(
                                value = editName,
                                onValueChange = { viewModel.editName.value = it },
                                placeholder = "Название вещи"
                            )
                        } else {
                            Text(
                                text = clothe.name.ifBlank { "—" },
                                color = TEXT_PRIMARY,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Colors
                    if (!clothe.colors.isNullOrEmpty()) {
                        AttributeSection(label = "Цвета") {
                            ColorChips(colors = clothe.colors)
                        }
                    }

                    // Category
                    clothe.category?.takeIf { it.isNotBlank() }?.let { category ->
                        AttributeSection(label = "Категория") {
                            Chip(text = category)
                        }
                    }

                    // Season
                    AttributeSection(label = "Сезон") {
                        if (isEditMode) {
                            EditField(
                                value = editSeason,
                                onValueChange = { viewModel.editSeason.value = it },
                                placeholder = "Лето / Зима / ..."
                            )
                        } else {
                            Text(
                                text = clothe.season ?: "—",
                                color = if (clothe.season != null) TEXT_PRIMARY else TEXT_SECONDARY,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Fit
                    AttributeSection(label = "Посадка") {
                        if (isEditMode) {
                            EditField(
                                value = editFit,
                                onValueChange = { viewModel.editFit.value = it },
                                placeholder = "Облегающий / Свободный / ..."
                            )
                        } else {
                            Text(
                                text = clothe.fit ?: "—",
                                color = if (clothe.fit != null) TEXT_PRIMARY else TEXT_SECONDARY,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Material
                    AttributeSection(label = "Материал") {
                        if (isEditMode) {
                            EditField(
                                value = editMaterial,
                                onValueChange = { viewModel.editMaterial.value = it },
                                placeholder = "Хлопок / Шерсть / ..."
                            )
                        } else {
                            Text(
                                text = clothe.material ?: "—",
                                color = if (clothe.material != null) TEXT_PRIMARY else TEXT_SECONDARY,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Brand
                    AttributeSection(label = "Бренд") {
                        if (isEditMode) {
                            EditField(
                                value = editBrand,
                                onValueChange = { viewModel.editBrand.value = it },
                                placeholder = "Zara / H&M / ..."
                            )
                        } else {
                            Text(
                                text = clothe.brand ?: "—",
                                color = if (clothe.brand != null) TEXT_PRIMARY else TEXT_SECONDARY,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Style tags
                    AttributeSection(label = "Стиль") {
                        if (isEditMode) {
                            EditField(
                                value = editStyleTags,
                                onValueChange = { viewModel.editStyleTags.value = it },
                                placeholder = "casual, street, formal"
                            )
                        } else {
                            val tags = clothe.styleTags
                                ?.split(",")
                                ?.map { it.trim() }
                                ?.filter { it.isNotBlank() }
                            if (!tags.isNullOrEmpty()) {
                                TagChips(tags = tags)
                            } else {
                                Text("—", color = TEXT_SECONDARY, fontSize = 15.sp)
                            }
                        }
                    }

                    // Marketplace URL section
                    Spacer(modifier = Modifier.height(4.dp))
                    MarketplaceSection(
                        storeUrl = clothe.storeUrl,
                        isEditMode = isEditMode,
                        editStoreUrl = editStoreUrl,
                        onEditStoreUrlChange = { viewModel.editStoreUrl.value = it },
                        onOpenStore = { url ->
                            if (url.isNotBlank()) uriHandler.openUri(url)
                        },
                        onActivateEdit = { viewModel.onEditToggle() }
                    )

                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // "Собрать образ" bottom button
            Button(
                onClick = { /* EPIC-16 */ },
                enabled = false,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ACCENT,
                    disabledBackgroundColor = Color(0xFF3A3A3A)
                )
            ) {
                Text(
                    text = "Собрать образ",
                    color = Color(0xFF9AA0A6),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun AttributeSection(label: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SURFACE)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = label, color = TEXT_SECONDARY, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        content()
    }
}

@Composable
private fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TEXT_SECONDARY, fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = TEXT_PRIMARY,
            unfocusedBorderColor = Color(0xFF555555),
            focusedBorderColor = ACCENT,
            cursorColor = ACCENT,
            backgroundColor = SURFACE
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorChips(colors: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { hex ->
            val color = runCatching { Color(parseHexColor(hex)) }.getOrElse { Color.Gray }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color(0xFF555555), CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagChips(tags: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.forEach { tag ->
            Chip(text = tag)
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CHIP_BG)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = TEXT_PRIMARY, fontSize = 13.sp)
    }
}

@Composable
private fun MarketplaceSection(
    storeUrl: String?,
    isEditMode: Boolean,
    editStoreUrl: String,
    onEditStoreUrlChange: (String) -> Unit,
    onOpenStore: (String) -> Unit,
    onActivateEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SURFACE)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Ссылка на магазин", color = TEXT_SECONDARY, fontSize = 12.sp, fontWeight = FontWeight.Medium)

        if (isEditMode) {
            OutlinedTextField(
                value = editStoreUrl,
                onValueChange = onEditStoreUrlChange,
                placeholder = { Text("https://www.wildberries.ru/...", color = TEXT_SECONDARY, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = TEXT_PRIMARY,
                    unfocusedBorderColor = Color(0xFF555555),
                    focusedBorderColor = ACCENT,
                    cursorColor = ACCENT,
                    backgroundColor = SURFACE
                ),
                singleLine = true
            )
        } else {
            val hasUrl = !storeUrl.isNullOrBlank()
            if (hasUrl) {
                val domain = extractDomain(storeUrl!!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = domain,
                        color = ACCENT,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ACCENT)
                            .clickable { onOpenStore(storeUrl) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Открыть", color = Color(0xFF27272B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Text(
                    text = "Добавить ссылку на магазин",
                    color = TEXT_SECONDARY,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onActivateEdit() }
                )
            }
        }
    }
}

private fun extractDomain(url: String): String {
    return url
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .substringBefore("/")
}

private fun parseHexColor(hex: String): Long {
    val cleaned = hex.removePrefix("#")
    return when (cleaned.length) {
        6 -> ("FF$cleaned").toLong(16)
        8 -> cleaned.toLong(16)
        else -> 0xFF888888L
    }
}
