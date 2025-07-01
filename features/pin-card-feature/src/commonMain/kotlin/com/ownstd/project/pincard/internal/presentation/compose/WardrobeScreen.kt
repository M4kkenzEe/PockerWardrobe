package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp)
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(clothes) { clothe ->
                val request = ImageRequest.Builder(context)
                    .data(clothe.imageUrl)
                    .httpHeaders(headers)
                    .build()

                ClotheCard(request)
            }

            items(if (clothes.size % 2 == 0) 2 else 3) {
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
                .padding(bottom = 60.dp)
                .padding(24.dp),
            onButtonClick = { bitmap -> viewModel.loadClothe(bitmap) }
        )
    }
}
