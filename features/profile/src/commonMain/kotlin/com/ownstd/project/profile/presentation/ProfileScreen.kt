package com.ownstd.project.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ownstd.project.profile.domain.User
import com.ownstd.project.core.resources.MR
import com.ownstd.project.profile.resources.Res
import com.ownstd.project.profile.resources.img_profile_default
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.resources.painterResource as composePainterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

private val BackgroundColor = Color(0xFFF5F4F2)
private val CardColor = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF9E9E9E)
private val DividerColor = Color(0xFFF0EFEB)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val user by viewModel.profileState.collectAsState()

    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    ProfileContent(user = user!!, onLogout = onLogout, onOpenSettings = onOpenSettings)
}

@Composable
private fun ProfileContent(
    user: User,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 80.dp)
    ) {
        ProfileTopBar(onSettingsClick = onOpenSettings)
        Spacer(modifier = Modifier.height(20.dp))
        ProfileAvatarSection(user = user)
        Spacer(modifier = Modifier.height(16.dp))
        ProfileStatsCard(user = user)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "НАСТРОЙКИ",
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProfileSettingsSection(onOpenSettings = onOpenSettings)
    }
}

@Composable
private fun ProfileTopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Профиль",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .background(CardColor, CircleShape)
        ) {
            Icon(
                painter = painterResource(MR.images.sliders_horizontal),
                contentDescription = "Настройки",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileAvatarSection(user: User) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = user.avatarUrl.ifEmpty { null },
                contentDescription = "Аватар",
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = composePainterResource(Res.drawable.img_profile_default),
                placeholder = composePainterResource(Res.drawable.img_profile_default),
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .background(Color(0xFF1A1A1A), CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = user.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "@${user.username}",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {},
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color(0xFFDDDDCF)),
            modifier = Modifier.fillMaxWidth(0.65f)
        ) {
            Text(
                text = "Редактировать профиль",
                color = TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ProfileStatsCard(user: User) {
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = CardColor,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStatItem(count = user.outfitsCount, label = "Образов")
            ProfileStatItem(count = user.clothesCount, label = "Вещей")
            ProfileStatItem(count = user.sharedCount, label = "Поделились")
        }
    }
}

@Composable
private fun ProfileStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun ProfileSettingsSection(onOpenSettings: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = CardColor,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            ProfileSettingsRow(
                icon = MR.images.star,
                title = "Мой стиль",
                subtitle = "Casual · Minimal"
            )
            Divider(
                color = DividerColor,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 52.dp)
            )
            ProfileSettingsRow(
                icon = MR.images.pencil,
                title = "Размеры",
                subtitle = "EU 42 · S"
            )
            Divider(
                color = DividerColor,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 52.dp)
            )
            ProfileSettingsRow(
                icon = MR.images.bell,
                title = "Уведомления",
                subtitle = null,
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun ProfileSettingsRow(
    icon: ImageResource,
    title: String,
    subtitle: String?,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = TextPrimary)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }
        Icon(
            painter = painterResource(MR.images.chevron_right),
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// region Previews

@Preview
@Composable
private fun ProfileContentPreview() {
    ProfileContent(
        user = User(
            name = "Sophia Laurent",
            email = "sophia@example.com",
            gender = Gender.FEMALE,
            username = "sophia.style",
            outfitsCount = 42,
            clothesCount = 167,
            sharedCount = 28,
        ),
        onLogout = {},
        onOpenSettings = {},
    )
}

@Preview
@Composable
private fun ProfileStatItemPreview() {
    ProfileStatItem(count = 42, label = "Образов")
}

@Preview
@Composable
private fun ProfileSettingsRowWithSubtitlePreview() {
    ProfileSettingsRow(
        icon = MR.images.pencil,
        title = "Размеры",
        subtitle = "EU 42 · S",
    )
}

@Preview
@Composable
private fun ProfileSettingsRowNoSubtitlePreview() {
    ProfileSettingsRow(
        icon = MR.images.bell,
        title = "Уведомления",
        subtitle = null,
    )
}

// endregion
