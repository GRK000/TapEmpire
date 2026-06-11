package com.example.juego.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.CometReward
import com.example.juego.ui.viewmodel.CometRewardType
import com.example.juego.ui.viewmodel.GoldenComet
import kotlinx.coroutines.delay

/**
 * Cometa Dorado: aparece unos segundos en una posición aleatoria.
 * Cazarlo otorga una recompensa aleatoria (frenesí, tap rush, monedas o gemas).
 */
@Composable
fun GoldenCometOverlay(
    comet: GoldenComet?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxW = maxWidth
        val maxH = maxHeight

        AnimatedVisibility(
            visible = comet != null,
            enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn(),
            exit = fadeOut(tween(250)),
            modifier = Modifier.offset(
                x = maxW * (comet?.xFraction ?: 0.5f) - 36.dp,
                y = maxH * (comet?.yFraction ?: 0.3f) - 36.dp
            )
        ) {
            val transition = rememberInfiniteTransition(label = "comet")
            val bob by transition.animateFloat(
                initialValue = -6f, targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "comet_bob"
            )
            val glow by transition.animateFloat(
                initialValue = 0.35f, targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700),
                    repeatMode = RepeatMode.Reverse
                ), label = "comet_glow"
            )
            val spin by transition.animateFloat(
                initialValue = -12f, targetValue = 12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "comet_spin"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer { translationY = bob; rotationZ = spin }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CoinGold.copy(alpha = glow * 0.6f),
                                CoinGold.copy(alpha = glow * 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTap
                    )
            ) {
                Text(text = "☄️", fontSize = 40.sp)
            }
        }
    }
}

/**
 * Banner que anuncia la recompensa del cometa. Se auto-descarta a los 3 segundos.
 */
@Composable
fun CometRewardBanner(
    reward: CometReward?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(reward) {
        if (reward != null) {
            delay(3000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = reward != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        reward?.let { r ->
            val text = when (r.type) {
                CometRewardType.FRENZY ->
                    stringResource(R.string.comet_reward_frenzy, r.amount.toInt(), r.durationSec)
                CometRewardType.TAP_RUSH ->
                    stringResource(R.string.comet_reward_tap, r.amount.toInt(), r.durationSec)
                CometRewardType.COIN_BURST ->
                    stringResource(R.string.comet_reward_coins, GameState.fmt(r.amount))
                CometRewardType.GEM_BONUS ->
                    stringResource(R.string.comet_reward_gems, r.amount.toInt())
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Onyx.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("☄️", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            stringResource(R.string.comet_caught_title),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoinGold
                        )
                        Text(
                            text,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chip que muestra un boost temporal activo con cuenta atrás.
 */
@Composable
fun BoostChip(
    emoji: String,
    multiplier: Double,
    remainingMs: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (remainingMs <= 0) return
    val seconds = (remainingMs / 1000).toInt() + 1
    Text(
        text = "$emoji ×${multiplier.toInt()} · ${seconds}s",
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
