package com.ownstd.project.profile.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ownstd.project.profile.resources.Res
import com.ownstd.project.profile.resources.img_profile_default
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = koinViewModel()
    val userState by viewModel.profileState.collectAsState()
    if (userState != null) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Фото пользователя в верхней части экрана
            Image(
                painter = painterResource(Res.drawable.img_profile_default),
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 200f
                        )
                    )
            )

            // Информация о пользователе поверх градиента
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = userState!!.name,
                    style = MaterialTheme.typography.h4,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userState!!.email,
                    style = MaterialTheme.typography.body1,
                    color = Color.White
                )
                Text(
                    text = "Gender: ${userState!!.gender}",
                    style = MaterialTheme.typography.body2,
                    color = Color.White
                )
            }
        }
    } else {
        Text("Loading...")
    }

    println("name: ${userState?.name}")
    println("email: ${userState?.email}")
    println("gender: ${userState?.gender}")
}
