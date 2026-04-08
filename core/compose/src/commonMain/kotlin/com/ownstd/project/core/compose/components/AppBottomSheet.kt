package com.ownstd.project.core.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import org.jetbrains.compose.ui.tooling.preview.Preview

private val SHEET_CORNER = 24.dp
private val HANDLE_WIDTH = 36.dp
private val HANDLE_HEIGHT = 4.dp
private val BACKDROP = Color.Black.copy(alpha = 0.4f)

/**
 * Универсальная нижняя шторка.
 * Размещается последним дочерним элементом в `Box(Modifier.fillMaxSize())` экрана,
 * чтобы перекрывать основной контент и Scaffold.
 */
@Composable
fun AppBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BACKDROP)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(300)) { it },
            exit = slideOutVertically(tween(250)) { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = SHEET_CORNER, topEnd = SHEET_CORNER))
                    .background(Theme.colors.background.primary)
                    .padding(bottom = 32.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(HANDLE_WIDTH)
                            .height(HANDLE_HEIGHT)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Theme.colors.stroke.primary),
                    )
                }

                if (title != null) {
                    Text(
                        text = title,
                        style = Theme.typography.subhead,
                        color = Theme.colors.label.primary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 4.dp),
                    )
                }

                content()
            }
        }
    }
}

@Preview
@Composable
private fun AppBottomSheetPreview() {
    AppBottomSheet(
        visible = true,
        onDismiss = {},
        title = "Заголовок шторки",
    ) {
        Text(
            text = "Содержимое шторки",
            style = Theme.typography.body,
            color = Theme.colors.label.secondary,
            modifier = Modifier.padding(16.dp),
        )
    }
}
