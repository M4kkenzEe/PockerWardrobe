package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.storage.EnvConfig
import com.ownstd.project.storage.TokenStorage
import kotlin.system.exitProcess
import org.koin.compose.koinInject

private val DARK_BG = Color(0xFF1A1A1A)
private val CARD_BG = Color(0xFF2A2A2A)
private val ACCENT_GREEN = Color(0xFF4CAF50)
private val ACCENT_ORANGE = Color(0xFFFF9800)
private val ACCENT_RED = Color(0xFFE53935)

@Composable
internal fun DebugScreen(onBack: () -> Unit = {}) {
    val tokenStorage: TokenStorage = koinInject()

    var isDev by remember { mutableStateOf(EnvConfig.isDev) }
    var showEnvConfirm by remember { mutableStateOf(false) }
    var pendingDev by remember { mutableStateOf(isDev) }

    if (showEnvConfirm) {
        AlertDialog(
            onDismissRequest = { showEnvConfirm = false },
            backgroundColor = CARD_BG,
            title = {
                Text(
                    text = "Сменить стенд?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Переключение на ${if (pendingDev) "DEV" else "PROD"} требует выхода из аккаунта и перезапуска приложения.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        EnvConfig.setDev(pendingDev)
                        tokenStorage.clearSession()
                        exitProcess(0)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = ACCENT_RED)
                ) {
                    Text("Выйти и перезапустить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnvConfirm = false }) {
                    Text("Отмена", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DARK_BG)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Назад", color = Color.White.copy(alpha = 0.7f))
            }
            Text(
                text = "Dev Tools",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Server environment section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CARD_BG)
                .padding(16.dp)
        ) {
            Text(
                text = "СЕРВЕР",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isDev) "DEV" else "PROD",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDev) ACCENT_ORANGE else ACCENT_GREEN
                    )
                    Text(
                        text = if (isDev) EnvConfig.DEV_URL else EnvConfig.PROD_URL,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = isDev,
                    onCheckedChange = { checked ->
                        pendingDev = checked
                        showEnvConfirm = true
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ACCENT_ORANGE,
                        checkedTrackColor = ACCENT_ORANGE.copy(alpha = 0.4f),
                        uncheckedThumbColor = ACCENT_GREEN,
                        uncheckedTrackColor = ACCENT_GREEN.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            EnvRow(label = "PROD", url = EnvConfig.PROD_URL, active = !isDev)
            Spacer(modifier = Modifier.height(6.dp))
            EnvRow(label = "DEV", url = EnvConfig.DEV_URL, active = isDev)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Смена стенда требует выхода из аккаунта",
                fontSize = 11.sp,
                color = ACCENT_RED.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun EnvRow(label: String, url: String, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) Color.White else Color.White.copy(alpha = 0.4f)
        )
        Text(
            text = url.removePrefix("https://").removePrefix("http://").removeSuffix("/api/v1/"),
            fontSize = 11.sp,
            color = if (active) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f)
        )
    }
}
