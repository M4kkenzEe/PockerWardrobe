package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
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
import coil3.compose.AsyncImage
import com.ownstd.project.pincard.internal.replaceFragment

@Composable
internal fun ClotheCard(
    clotheUrl: String = "",
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    var dropDownMenuState by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(20))
            .background(Color.White)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { dropDownMenuState = true }
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = clotheUrl,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
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
                    onDelete()
                    dropDownMenuState = false
                }
            ) { Text("Удалить") }
        }
    }
}

@Composable
internal fun LookCard(
    lookUrl: String,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    var dropDownMenuState by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .height(300.dp)
            .clip(RoundedCornerShape(20))
            .background(Color.White)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { dropDownMenuState = true }
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = lookUrl.replaceFragment(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
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
                    onDelete()
                    dropDownMenuState = false
                }
            ) { Text("Удалить") }
        }
    }
}
