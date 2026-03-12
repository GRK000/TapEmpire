package com.example.juego.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.SpecialEvent
import com.example.juego.ui.theme.*

@Composable
fun EventBanner(
    event: SpecialEvent?,
    modifier: Modifier = Modifier
) {
    val isActive = event != null && event.isActive

    AnimatedVisibility(
        visible = isActive,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        if (event != null) {
            val pulse by rememberInfiniteTransition(label = "event_pulse")
                .animateFloat(
                    initialValue = 1f,
                    targetValue = 1.03f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "event_scale"
                )

            GlassCard(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .scale(pulse),
                glassColor = NeonCyan.copy(alpha = 0.15f),
                borderColor = NeonCyan.copy(alpha = 0.4f),
                cornerRadius = 12.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${event.type.emoji} ${event.type.name} ${event.type.emoji}",
                        color = NeonCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "⏱️ ${event.remainingTime}s",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
