package com.example.juego.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.ComboSystem
import com.example.juego.ui.theme.*

@Composable
fun TapButton(
    comboEffect: ComboSystem.ComboEffect,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "tap_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val glowColor = when (comboEffect) {
        ComboSystem.ComboEffect.LEGENDARY -> ComboLegendary
        ComboSystem.ComboEffect.RAINBOW -> ComboRainbow
        ComboSystem.ComboEffect.LIGHTNING -> ComboLightning
        ComboSystem.ComboEffect.FIRE -> ComboFire
        ComboSystem.ComboEffect.BASIC -> ComboBasic
        else -> NeonPurple
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(180.dp)
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale * (1f + glowAlpha * 0.08f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = glowAlpha * 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .shadow(
                    elevation = if (isPressed) 4.dp else 16.dp,
                    shape = CircleShape,
                    ambientColor = glowColor.copy(alpha = 0.5f),
                    spotColor = glowColor.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.9f),
                            glowColor.copy(alpha = 0.6f),
                            glowColor.copy(alpha = 0.3f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onTap
                )
        ) {
            Text(
                text = "💰",
                fontSize = 52.sp
            )
        }
    }
}
