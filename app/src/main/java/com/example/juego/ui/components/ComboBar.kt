package com.example.juego.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.ui.theme.*

@Composable
fun ComboBar(
    comboCount: Int,
    comboMultiplier: Double,
    comboActive: Boolean,
    comboTimePercent: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = comboActive && comboCount > 1,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        val pulseScale by rememberInfiniteTransition(label = "combo_pulse")
            .animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "combo_scale"
            )

        val comboColor = when {
            comboCount >= 50 -> ComboLegendary
            comboCount >= 30 -> ComboRainbow
            comboCount >= 15 -> ComboLightning
            comboCount >= 5 -> ComboFire
            else -> ComboBasic
        }

        val comboText = when {
            comboCount >= 50 -> "🌟 LEGENDARY! 🌟"
            comboCount >= 30 -> "🌈 RAINBOW! 🌈"
            comboCount >= 15 -> "⚡ LIGHTNING! ⚡"
            comboCount >= 5 -> "🔥 ON FIRE! 🔥"
            else -> "COMBO!"
        }

        GlassCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .scale(pulseScale),
            glassColor = comboColor.copy(alpha = 0.15f),
            borderColor = comboColor.copy(alpha = 0.4f),
            cornerRadius = 12.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comboText,
                    color = comboColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "x$comboCount",
                    color = comboColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { comboTimePercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = comboColor,
                trackColor = comboColor.copy(alpha = 0.2f)
            )
        }
    }
}
