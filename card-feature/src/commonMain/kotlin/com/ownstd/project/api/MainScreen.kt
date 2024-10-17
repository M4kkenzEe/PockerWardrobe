package com.ownstd.project.api

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
import androidx.navigation.compose.rememberNavController
import com.ownstd.project.design_system.BLUE_COLOR
import com.ownstd.project.design_system.DARK_GREY_COLOR
import com.ownstd.project.design_system.GREY_COLOR
import com.ownstd.project.internal.navigation.BottomNavigationItems
import com.ownstd.project.internal.navigation.BottomNavigationNavHost
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        modifier = Modifier.fillMaxSize(),
    ) {
        BottomNavigationNavHost(navController)
    }
}

@Composable
internal fun BottomNavigationBar(navController: NavHostController) {

    var currentItem by remember { mutableStateOf(BottomNavigationItems.HOME) }

    Row(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .height(58.dp)
            .background(DARK_GREY_COLOR),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        for (item in BottomNavigationItems.entries) {
            BottomNavigationItem(
                painter = painterResource(
                    if (currentItem.route == item.route) item.enabledIcon
                    else item.disabledIcon
                ),
                label = item.label,
                textColor = if (currentItem.route == item.route) BLUE_COLOR else GREY_COLOR ,
                onItemClick = {
                    navController.navigate(item.route)
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
