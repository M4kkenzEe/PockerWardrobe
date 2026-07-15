package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ownstd.project.designsystem.components.SkeletonCard
import com.ownstd.project.designsystem.components.rememberShimmerTranslation
import com.ownstd.project.pincard.internal.replaceFragment

@Composable
internal fun ClotheCard(
    clotheUrl: String = "",
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dropDownMenuState by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(20))
            .background(Color(0xFFF0EEE9))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { dropDownMenuState = true }
            ),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = clotheUrl,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            loading = {
                val shimmer by rememberShimmerTranslation()
                SkeletonCard(
                    shimmerTranslation = shimmer,
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 160.dp),
                    shape = RoundedCornerShape(20),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 160.dp)
        )
        DropdownMenu(
            expanded = dropDownMenuState,
            onDismissRequest = { dropDownMenuState = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    onShare()
                    dropDownMenuState = false
                }
            ) { Text("Поделиться") }

            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .background(Color.Gray)
            )

            DropdownMenuItem(
                onClick = {
                    showConfirmDialog = true
                    dropDownMenuState = false
                }
            ) { Text("Удалить") }
        }
    }

    if (showConfirmDialog) {
        ConstructorAlertDialog(
            title = "Удалить вещь?",
            message = "Вещь будет удалена из гардероба",
            confirmText = "Удалить",
            cancelText = "Отмена",
            onConfirm = {
                showConfirmDialog = false
                onDelete()
            },
            onCancel = { showConfirmDialog = false }
        )
    }
}

@Composable
internal fun LookCard(
    lookUrl: String,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dropDownMenuState by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(300.dp)
            .clip(RoundedCornerShape(20))
            .background(Color(0xFFF0EEE9))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { dropDownMenuState = true }
            ),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = lookUrl.replaceFragment(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            loading = {
                val shimmer by rememberShimmerTranslation()
                SkeletonCard(
                    shimmerTranslation = shimmer,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20),
                )
            },
            modifier = Modifier
        )

        DropdownMenu(
            expanded = dropDownMenuState,
            onDismissRequest = { dropDownMenuState = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    onShare()
                    dropDownMenuState = false
                }
            ) { Text("Поделиться") }

            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .background(Color.Gray)
            )

            DropdownMenuItem(
                onClick = {
                    showConfirmDialog = true
                    dropDownMenuState = false
                }
            ) { Text("Удалить") }
        }
    }

    if (showConfirmDialog) {
        ConstructorAlertDialog(
            title = "Удалить образ?",
            message = "Образ будет удалён",
            confirmText = "Удалить",
            cancelText = "Отмена",
            onConfirm = {
                showConfirmDialog = false
                onDelete()
            },
            onCancel = { showConfirmDialog = false }
        )
    }
}
