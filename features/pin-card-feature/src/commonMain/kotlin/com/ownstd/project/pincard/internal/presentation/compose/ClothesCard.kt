package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest


private const val RED_SWEATER =
    "https://img2.freepng.ru/20180602/cbc/kisspng-sweater-bluza-clothing-choker-wool-sweater-5b130b017d0a61.0627974415279746575122.jpg"

@Composable
fun SweaterScreen() {
    val itemsCount = 13
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(itemsCount) {
//            ClotheCard(imgUrl = RED_SWEATER)
        }

        items(if (itemsCount % 2 == 0) 2 else 3) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }
    }
}

@Composable
internal fun ClotheCard(imageRequest: ImageRequest) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(20))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
        )
    }
}

//@Composable
//internal fun ClotheCard1(imageLoader: ImageLoader, imgUrl: String = RED_SWEATER) {
//    Box(
//        modifier = Modifier
//            .size(160.dp)
//            .clip(RoundedCornerShape(20))
//            .background(Color.White),
//        contentAlignment = Alignment.Center
//    ) {
//        AsyncImage(
//            model = imgUrl,
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            imageLoader = imageLoader
//        )
//        Text("Sweater", color = Color.Black)
//    }
//}