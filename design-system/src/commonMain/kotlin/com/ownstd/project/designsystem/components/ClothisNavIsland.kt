package com.ownstd.project.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.ownstd.project.designsystem.theme.ClothisTheme
import kotlinprojecttesting.design_system.generated.resources.Res
import kotlinprojecttesting.design_system.generated.resources.ic_checkroom
import kotlinprojecttesting.design_system.generated.resources.ic_person
import kotlinprojecttesting.design_system.generated.resources.ic_person_fill
import org.jetbrains.compose.resources.painterResource

enum class NavTab { Wardrobe, Profile }

@Composable
fun ClothisNavIsland(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
    applyInsets: Boolean = true,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens

    // TODO: Replace background with Haze backdrop-blur using chrisbanes/haze library.
    // glassTint color approximates the glass look without real blur.
    val islandShape = RoundedCornerShape(dimens.radiusFull)

    Row(
        modifier = modifier
            .then(
                if (applyInsets) Modifier
                    .navigationBarsPadding()
                    .padding(bottom = dimens.navIslandBottomGap)
                else Modifier
            )
            .width(240.dp)
            .height(dimens.navIslandHeight)
            .clip(islandShape)
            .background(colors.glassTint)
            .border(width = 1.dp, color = colors.glassStroke, shape = islandShape)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavIslandTab(
            label = "Гардероб",
            painter = painterResource(Res.drawable.ic_checkroom),
            isActive = selectedTab == NavTab.Wardrobe,
            onClick = { onTabSelected(NavTab.Wardrobe) },
        )
        NavIslandTab(
            label = "Профиль",
            painter = if (selectedTab == NavTab.Profile)
                painterResource(Res.drawable.ic_person_fill)
            else
                painterResource(Res.drawable.ic_person),
            isActive = selectedTab == NavTab.Profile,
            onClick = { onTabSelected(NavTab.Profile) },
        )
    }
}

@Composable
private fun NavIslandTab(
    label: String,
    painter: Painter,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val colors = ClothisTheme.colors
    val typography = ClothisTheme.typography
    val dimens = ClothisTheme.dimens
    val tint = if (isActive) colors.clay else colors.onCanvasMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp, horizontal = 12.dp),
    ) {
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = label,
            modifier = Modifier.size(dimens.navIslandIconSize),
            colorFilter = ColorFilter.tint(tint),
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (isActive) {
            // Active state: clay dot below icon, then clay label
            Box(
                modifier = Modifier
                    .size(dimens.navIslandDotSize)
                    .clip(CircleShape)
                    .background(colors.clay),
            )
        }

        Text(
            text = label,
            style = typography.label,
            color = tint,
        )
    }
}
