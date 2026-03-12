package com.example.juego.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.Pet
import com.example.juego.ui.theme.*

@Composable
fun PetWidget(
    pet: Pet?,
    modifier: Modifier = Modifier
) {
    if (pet == null || !pet.isOwned) return

    val infiniteTransition = rememberInfiniteTransition(label = "pet_bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet_y"
    )

    GlassCard(
        modifier = modifier.width(80.dp),
        cornerRadius = 12.dp,
        glassColor = NeonPurple.copy(alpha = 0.1f),
        borderColor = NeonPurple.copy(alpha = 0.2f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val emoji = if (pet.isAlive) pet.type.emoji else if (pet.isGhost) "👻" else "💀"
            Text(
                text = emoji,
                fontSize = 28.sp,
                modifier = Modifier.offset(y = bounce.dp)
            )
            Text(
                text = pet.statusEmoji,
                fontSize = 14.sp
            )
            Text(
                text = pet.customName ?: pet.type.name,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}
