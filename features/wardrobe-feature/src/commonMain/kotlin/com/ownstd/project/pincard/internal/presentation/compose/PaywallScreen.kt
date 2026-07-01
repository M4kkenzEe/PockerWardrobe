package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@Composable
internal fun PaywallScreen(onDismiss: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = Color(0xFF9AA0A6)
                        ),
                        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                    ) {
                        Text("✕", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "✨",
                    fontSize = 56.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Вы достигли лимита",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Бесплатный план позволяет хранить до 100 вещей.\nОформите Pro для неограниченного гардероба.",
                    fontSize = 14.sp,
                    color = Color(0xFF9AA0A6),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TierCard(
                        title = "Free",
                        price = "Бесплатно",
                        features = listOf("До 100 вещей", "AI генерация образов"),
                        isHighlighted = false,
                        modifier = Modifier.weight(1f)
                    )
                    TierCard(
                        title = "Pro",
                        price = "$5.99/мес",
                        features = listOf("Без ограничений", "Приоритетный AI", "Ранний доступ"),
                        isHighlighted = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Скоро будет доступно")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8AB4F8))
                ) {
                    Text(
                        text = "Оформить подписку",
                        color = Color(0xFF27272B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = Color(0xFF9AA0A6)
                    ),
                    elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                ) {
                    Text(
                        text = "Продолжить бесплатно",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun TierCard(
    title: String,
    price: String,
    features: List<String>,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isHighlighted) Color(0xFF8AB4F8) else Color(0xFF3A3A3A)
    val bgColor = if (isHighlighted) Color(0xFF1A2332) else Color(0xFF1E1E1E)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isHighlighted) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF8AB4F8))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ПОПУЛЯРНЫЙ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF27272B)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = price,
            fontSize = 14.sp,
            color = if (isHighlighted) Color(0xFF8AB4F8) else Color(0xFF9AA0A6),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        features.forEach { feature ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                Text(
                    text = "✓",
                    fontSize = 12.sp,
                    color = if (isHighlighted) Color(0xFF8AB4F8) else Color(0xFF9AA0A6)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = feature,
                    fontSize = 12.sp,
                    color = Color(0xFFCCCCCC)
                )
            }
        }
    }
}
