package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ownstd.project.pincard.internal.presentation.viewmodel.ConstructorViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun ConstructorScreen() {
    val viewModel = koinViewModel<ConstructorViewModel>()
    val photoList by viewModel.clotheList.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { viewModel.addClothe() }) { Text("ADD") }
        Box(modifier = Modifier.fillMaxSize()) {
            photoList.forEach {
                PhotoItem(
                    img = it,
                    modifier = Modifier,
                )
            }
        }
    }
}

@Composable
fun PhotoItem(
    img: DrawableResource,
    modifier: Modifier = Modifier,
) {
    var offsetX by remember { mutableStateOf(100f) }
    var offsetY by remember { mutableStateOf(100f) }
    var isCardClicked by remember { mutableStateOf(false) }
    var imageSize by remember { mutableStateOf(250.dp) }
    var rotationAngle by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .border(
                BorderStroke(
                    width = 2.dp,
                    color = if (isCardClicked) Color.White else Color.Transparent,
                )
            )
            .wrapContentSize()
            .clickable { isCardClicked = !isCardClicked }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomChange, rotationChange ->
                    // Обновляем позицию
                    offsetX += pan.x
                    offsetY += pan.y

                    if (isCardClicked) {
                        imageSize = maxOf(20.dp, imageSize * zoomChange)
                    }
                    println("RRR: $rotationChange")
                    rotationAngle += rotationChange
                }
            },
    ) {
        Image(
            modifier = Modifier.size(imageSize).graphicsLayer(rotationZ = rotationAngle),
            painter = painterResource(img),
            contentDescription = null,
        )
    }
}
