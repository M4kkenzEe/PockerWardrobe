package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.parseToHost
import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import com.ownstd.project.storage.TokenStorage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WardrobeScreen() {
    val viewModel: WardrobeViewModel = koinViewModel()
    val clothes by viewModel.clothes.collectAsState()
    val storage: TokenStorage = koinInject()
    val context = LocalPlatformContext.current
    val headers = NetworkHeaders.Builder()
        .set("Authorization", "Bearer ${storage.getToken()}")
        .build()

    val networkRepository: NetworkRepository = koinInject()
    val baseUrl = networkRepository.baseUrl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 44.dp)
    ) {

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalItemSpacing = 12.dp
        ) {
            items(clothes) { clothe ->
                val request = ImageRequest.Builder(context)
                    .data(clothe.imageUrl.parseToHost(baseUrl))
                    .httpHeaders(headers)
                    .build()

                println("imageUrl: ${clothe.imageUrl.parseToHost(baseUrl)}")

                ClotheCard(request)
            }

            val count = if (clothes.size % 2 == 0) 2 else 3
            items(count) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
            }
        }
        AddClotheFloatButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            onButtonClick = { bitmap -> viewModel.loadClothe(bitmap) }
        )
    }
}
