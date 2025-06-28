package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ownstd.project.card.design_system.BG_GREY_COLOR
import com.ownstd.project.card.design_system.BLUE_COLOR
import com.ownstd.project.card.design_system.DARK_GREY_COLOR
import com.ownstd.project.card.design_system.GREY_COLOR
import com.ownstd.project.card.internal.navigation.BottomNavigationNavHost
import com.ownstd.project.card.internal.navigation.BottomNavigationScreens
import kotlinprojecttesting.features.card_feature.generated.resources.Res
import kotlinprojecttesting.features.card_feature.generated.resources.shopping_cart_disabled
import kotlinprojecttesting.features.card_feature.generated.resources.shopping_cart_enabled
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MainScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        modifier = Modifier.fillMaxSize(),
        backgroundColor = BG_GREY_COLOR
    ) {
        BottomNavigationNavHost(navController, modifier = Modifier.padding(bottom = 40.dp))
    }
}

@Composable
internal fun BottomNavigationBar(navController: NavHostController) {
    var currentItem by remember { mutableStateOf<BottomNavigationScreens>(BottomNavigationScreens.Home()) }

    Row(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .height(58.dp)
            .background(DARK_GREY_COLOR),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val screens = listOf(
            BottomNavigationScreens.Home(),
            BottomNavigationScreens.Shop(),
            BottomNavigationScreens.Outfits(),
            BottomNavigationScreens.Profile()
        )
        for (item in screens) {
            BottomNavigationItem(
                painter = painterResource(
                    if (currentItem == item)
                        mapper(item.enabledIcon)
                    else
                        mapper(item.disabledIcon)
                ),
                label = item.label,
                textColor = if (currentItem == item) BLUE_COLOR else GREY_COLOR,
                onItemClick = {
                    navController.navigate(item)
                    currentItem = item
                }
            )
        }
    }
}

@Composable
internal fun BottomNavigationItem(
    painter: Painter,
    label: String,
    textColor: Color,
    onItemClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 10.dp, bottom = 8.dp)
            .clickable { onItemClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Image(painter = painter, contentDescription = "")
        Text(
            text = label,
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

fun mapper(name: String): DrawableResource {
    return when (name) {
        "shopping_cart_enabled" -> Res.drawable.shopping_cart_enabled
        "shopping_cart_disabled" -> Res.drawable.shopping_cart_disabled
        // Добавьте другие ресурсы по мере необходимости
        else -> throw IllegalArgumentException("Unknown resource name: $name")
    }
}
