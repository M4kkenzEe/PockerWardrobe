package com.ownstd.project.wardrobe.internal.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.components.AppBottomSheet
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun AddBottomSheet(
    visible: Boolean,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onConstructorClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
    ) {
        AddAction(
            icon = MR.images.camera,
            title = "Сфотографировать вещь",
            subtitle = "AI удалит фон автоматически",
            onClick = onCameraClick,
        )
        Divider(
            color = Theme.colors.stroke.primary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        AddAction(
            icon = MR.images.image,
            title = "Добавить из галереи",
            subtitle = "Выберите фото из библиотеки",
            onClick = onGalleryClick,
        )
        Divider(
            color = Theme.colors.stroke.primary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        AddAction(
            icon = MR.images.sparkles,
            title = "Конструктор образов",
            subtitle = "Создайте образ из вещей",
            onClick = onConstructorClick,
        )
    }
}

@Composable
private fun AddAction(
    icon: ImageResource,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Theme.colors.background.surfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Theme.colors.label.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = Theme.typography.body,
                color = Theme.colors.label.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = Theme.typography.caption,
                color = Theme.colors.label.secondary,
            )
        }
    }
}

@Preview
@Composable
private fun AddBottomSheetPreview() {
    AddBottomSheet(
        visible = true,
        onCameraClick = {},
        onGalleryClick = {},
        onConstructorClick = {},
        onDismiss = {},
    )
}
