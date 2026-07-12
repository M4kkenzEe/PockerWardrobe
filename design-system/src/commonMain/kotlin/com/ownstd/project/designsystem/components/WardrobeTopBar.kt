package com.ownstd.project.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ownstd.project.designsystem.theme.ClothisTheme
import kotlinprojecttesting.design_system.generated.resources.Res
import kotlinprojecttesting.design_system.generated.resources.ic_search
import kotlinprojecttesting.design_system.generated.resources.ic_tune
import org.jetbrains.compose.resources.painterResource

@Composable
fun WardrobeTopBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = dimens.screenHorizontalPadding)
            .padding(top = dimens.spaceMd, bottom = dimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ClothisIconButton(
            painter = painterResource(Res.drawable.ic_search),
            contentDescription = "Поиск",
            onClick = onSearchClick,
        )

        WardrobeSegmentedControl(
            tabs = listOf("Вещи", "Образы"),
            selectedIndex = selectedTab,
            onTabSelected = onTabSelected,
        )

        ClothisIconButton(
            painter = painterResource(Res.drawable.ic_tune),
            contentDescription = "Фильтр",
            onClick = onFilterClick,
        )
    }
}

@Composable
private fun WardrobeSegmentedControl(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ClothisTheme.colors
    val typography = ClothisTheme.typography
    val dimens = ClothisTheme.dimens
    val trackShape = RoundedCornerShape(dimens.radiusFull)
    val segmentShape = RoundedCornerShape(dimens.radiusFull)

    Box(
        modifier = modifier
            .width(dimens.segmentControlWidth)
            .height(dimens.segmentControlHeight)
            .clip(trackShape)
            .background(colors.segmentTrack)
            .padding(4.dp),
    ) {
        Row(modifier = Modifier.matchParentSize()) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(dimens.segmentControlHeight - 8.dp)
                        .clip(segmentShape)
                        .background(if (isSelected) colors.segmentActive else Color.Transparent)
                        .shadow(
                            elevation = if (isSelected) 2.dp else 0.dp,
                            shape = segmentShape,
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab,
                        style = typography.label,
                        color = if (isSelected) colors.onCanvas else colors.onCanvasMuted,
                    )
                }
            }
        }
    }
}
