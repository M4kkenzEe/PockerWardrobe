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
import kotlin.system.exitProcess

private val DARK_BG = Color(0xFF1A1A1A)
private val CARD_BG = Color(0xFF2A2A2A)
private val ACCENT = Color(0xFF4CAF50)
private val DEV_COLOR = Color(0xFFFF9800)

@Composable
internal fun DebugScreen(onBack: () -> Unit = {}) {
    var isDev by remember { mutableStateOf(EnvConfig.isDev) }
    var pendingRestart by remember { mutableStateOf(false) }

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

        // Current env indicator
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
                        color = if (isDev) DEV_COLOR else ACCENT
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
                        isDev = checked
                        EnvConfig.setDev(checked)
                        pendingRestart = true
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DEV_COLOR,
                        checkedTrackColor = DEV_COLOR.copy(alpha = 0.4f),
                        uncheckedThumbColor = ACCENT,
                        uncheckedTrackColor = ACCENT.copy(alpha = 0.4f)
                    )
                )
            }

            if (pendingRestart) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { exitProcess(0) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isDev) DEV_COLOR else ACCENT,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Перезапустить приложение",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Env URLs reference
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CARD_BG)
                .padding(16.dp)
        ) {
            Text(
                text = "СТЕНДЫ",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            EnvRow(label = "PROD", url = EnvConfig.PROD_URL, active = !isDev)
            Spacer(modifier = Modifier.height(6.dp))
            EnvRow(label = "DEV", url = EnvConfig.DEV_URL, active = isDev)
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
