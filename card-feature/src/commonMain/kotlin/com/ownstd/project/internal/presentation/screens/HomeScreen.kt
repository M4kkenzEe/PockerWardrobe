package com.ownstd.project.internal.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.internal.presentation.viewmodel.FirstViewModel
import com.ownstd.project.resources.Res
import com.ownstd.project.resources.shopping_cart_enabled
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
internal fun HomeScreen(navigateTo: () -> Unit = {}) {
    val viewModel = koinViewModel<FirstViewModel>()
    val st by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(Res.drawable.shopping_cart_enabled), contentDescription = null)
        Icon(painter = painterResource(Res.drawable.shopping_cart_enabled), contentDescription = null)
        Text("HOME $st")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = navigateTo) {
            Text("Go to Second Screenjjj")
        }
    }
}