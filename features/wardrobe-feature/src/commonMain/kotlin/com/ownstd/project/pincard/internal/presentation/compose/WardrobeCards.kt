package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.parseToHost
import com.ownstd.project.storage.TokenStorage
import org.koin.compose.koinInject

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

@Composable
internal fun LookCard(lookUrl: String) {
    val context = LocalPlatformContext.current
    val storage: TokenStorage = koinInject()
    val headers = NetworkHeaders.Builder()
        .set("Authorization", "Bearer ${storage.getToken()}")
        .build()
    val networkRepository: NetworkRepository = koinInject()
    val baseUrl = networkRepository.baseUrl

    Box(
        modifier = Modifier
            .height(300.dp)
            .clip(RoundedCornerShape(20))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        val request = ImageRequest.Builder(context)
            .data(lookUrl.parseToHost(baseUrl))
            .httpHeaders(headers)
            .crossfade(true)
            .precision(Precision.EXACT)
            .build()

        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
        )

    }
}
